/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;

/**
 * 补单服务
 * 
 * @author tony 2016年8月14日 下午4:08:35
 *
 */
@Component
public class CompensateService implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CompensateService.class);

    private CloseableHttpAsyncClient httpClient;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private CallbackServiceImpl callbackServiceImpl;


    /**
     * 初始化信号量
     */
    public void initSemaphore() {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for (AppConfig appConfig : allAppConfig) {
            if (!StringUtils.isEmpty(appConfig.getDispatchGroup())) {
                for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {
                    initSemaphore(messageConfig);
                }
            }
        }
    }

    /**
     * 初始化信号量，供调度使用
     * 
     * @param messageConfig
     */
    private void initSemaphore(MessageConfig messageConfig) {
        for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
            String consumerId = callbackConfig.getCallbackKey();
            int parallelismNum =
                    (callbackConfig.getParallelismNum() == null || callbackConfig.getParallelismNum().intValue() < 2)
                            ? 2 : callbackConfig.getParallelismNum().intValue();
            AdjustableSemaphore semaphore = SemaphorManager.get(consumerId);
            if (semaphore == null) {
                semaphore = new AdjustableSemaphore(parallelismNum);
                SemaphorManager.put(consumerId, semaphore);
            } else {
                semaphore.setMaxPermits(parallelismNum);
            }
        }
    }

    /**
     * 检测补单合并逻辑，供调度使用
     */
    public void checkAndCompensate() {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for (AppConfig appConfig : allAppConfig) {
            if (!StringUtils.isEmpty(appConfig.getDispatchGroup())) {
                for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {
                    if (!Boolean.FALSE.equals(messageConfig.getEnable())) {

                        String appId = appConfig.getAppId();
                        String code = messageConfig.getCode();

                        logger.info("check and compensate Start, appId:{}, code:{}.", appId, code);

                        logger.info("STEP.1 checkToCompensate");
                        checkToCompensate(appId, code);

                        logger.info("STEP.2 compensate");
                        compensate(appId, code);

                        logger.info("check and compensate end.");
                    }
                }
            }
        }
    }

    /**
     * 检测出需要补偿的消息写入补单库
     */
    public void checkToCompensate(String appId, String code) {
        AppConfig appConfig = appConfigRepository.getAppConfig(appId);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + appId);
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(code);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + code);
        }

        if (messageConfig.getCallbackCfgList() == null || messageConfig.getCallbackCfgList().size() == 0) {
            throw new BizException(ErrorCode.NOT_EXIST_INVALID_CALLBACK,
                    String.format("appid:%s, code:%s", appId, code));
        }

        List<Message> needToCompensate = messageRepository.getNeedToCompensate(appId, code);
        if (needToCompensate != null && needToCompensate.size() > 0) {
            logger.error("check need to compensate,appId{}, code:{}, num:{}", appId, code, needToCompensate.size());

            for (Message message : needToCompensate) {
                for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
                    MessageCompensate messageCompensate =
                            MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Compensate);
                    messageCompensateRepository.insert(messageCompensate);
                }
                messageRepository.updateMessageStatus(appId, code, message.getUuid(),
                        MessageNewStatusEnum.CheckToCompensate, MessageProcessStatusEnum.Init);
            }
        }
    }

    /**
     * 根据Appid和Code进行补单
     */
    public void compensate(String appId, String code) {
        AppConfig appConfig = appConfigRepository.getAppConfig(appId);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + appId);
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(code);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + code);
        }


        List<MessageCompensate> messageCompensatesList = messageCompensateRepository.getNeedCompensate(appId, code);
        if (messageCompensatesList.size() > 0) {
            logger.info("find need to compensate,appId:{}, code:{}, num:{}", appId, code,
                    messageCompensatesList.size());
            for (MessageCompensate messageCompensate : messageCompensatesList) {
                try {
                    compensateCallback(messageCompensate, messageConfig);
                } catch (Exception e) {
                    logger.error(String.format("message compensate failed with appId:%s, code:%s, messageId:%s.", appId,
                            code, messageCompensate.getMessageId()), e);
                }
            }
        }
    }

    /**
     * 补单回调
     * 
     * @param messageCompensate
     */
    private void compensateCallback(MessageCompensate messageCompensate, MessageConfig messageConfig) {
        String appId = messageCompensate.getAppId();
        String code = messageCompensate.getCode();
        String uuid = messageCompensate.getMessageUuid();

        CallbackConfig callbackConfig = messageConfig.getCallbackConfig(messageCompensate.getConsumerId());
        Message message = messageRepository.getByUuid(appId, code, uuid);

        try {
            new BizSystemCallback(httpClient, message, messageCompensate, callbackConfig, callbackServiceImpl).send();
        } catch (Exception e) {
            logger.error(String.format("compensate call biz system fail,appCode:%s, messageUuid:%s",
                    message.getAppCode(), message.getUuid()), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        httpClient = HttpAsyncClients.custom().setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(cm).build();
        httpClient.start();
    }
}
