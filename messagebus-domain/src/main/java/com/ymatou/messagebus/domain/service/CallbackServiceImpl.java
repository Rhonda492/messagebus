/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.Date;
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
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.domain.repository.AlarmRepository;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusSourceEnum;
import com.ymatou.messagebus.infrastructure.logger.ErrorReportClient;
import com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService;

/**
 * 回调服务
 * 
 * @author wangxudong 2016年8月5日 下午7:03:35
 *
 */
@Component
public class CallbackServiceImpl implements CallbackService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

    private CloseableHttpAsyncClient httpClient;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private AlarmRepository alarmRepository;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private ErrorReportClient errorReportClient;

    @Resource
    private TaskExecutor taskExecutor;

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService#invoke(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void invoke(String exchange, String queue, String messageBody, String messageId, String messageUuid) {
        AppConfig appConfig = appConfigRepository.getAppConfig(exchange);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + exchange);
        }

        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(queue);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appCode:" + queue);
        }

        List<CallbackConfig> callbackCfgList = messageConfig.getCallbackCfgList();
        if (callbackCfgList == null
                || !callbackCfgList.stream().anyMatch(x -> x.getEnable() == null || x.getEnable() == true)) {
            throw new BizException(ErrorCode.NOT_EXIST_INVALID_CALLBACK, "appCode:" + queue);
        }

        if (StringUtils.isEmpty(messageId)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageId can not be empty.");
        }

        if (StringUtils.isEmpty(messageUuid)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageUuid can not be empty.");
        }

        Message message = new Message();
        message.setAppId(appConfig.getAppId());
        message.setCode(messageConfig.getCode());
        message.setBody(messageBody);
        message.setMessageId(messageId);
        message.setUuid(messageUuid);

        invokeCore(message, messageConfig);
    }

    /**
     * 回调核心逻辑
     * 
     * @param message
     * @param messageConfig
     */
    private void invokeCore(Message message, MessageConfig messageConfig) {
        for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
            if (callbackConfig.getEnable() == null || callbackConfig.getEnable() == true) {

                try {
                    new BizSystemCallback(httpClient, message, null, callbackConfig, this).send();
                } catch (Exception e) {
                    logger.error(String.format("invoke biz system fail,appCode:%s, messageUuid:%s",
                            message.getAppCode(), message.getUuid()), e);
                }
            }
        }
    }

    /**
     * 回写成功结果
     * 
     * @param message
     * @param callbackConfig
     * @param duration
     */
    public void writeSuccessResult(Message message, MessageCompensate messageCompensate, CallbackConfig callbackConfig,
            long duration) {
        String requestId = MDC.get("logPrefix");

        taskExecutor.execute(() -> {
            MDC.put("logPrefix", requestId);

            logger.info("----------------------- callback write success message begin ----------------");
            try {
                MessageStatus messageStatus = MessageStatus.from(message, callbackConfig);
                if (messageCompensate == null) {
                    messageStatus.setSource(MessageStatusSourceEnum.Dispatch.toString());
                } else {
                    messageStatus.setSource(MessageStatusSourceEnum.Compensate.toString());
                }
                messageStatus.setStatus(MessageStatusEnum.PushOk.toString());
                messageStatus.setSuccessResult(callbackConfig.getCallbackKey(), duration, callbackConfig.getUrl());

                messageStatusRepository.insert(messageStatus, message.getAppId());

                if (messageCompensate != null) {
                    messageCompensate.incRetryCount();
                    messageCompensate.setNewStatus(MessageCompensateStatusEnum.RetryOk.code());
                    messageCompensateRepository.update(messageCompensate);
                }

                // TODO 处理多条结果
                messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(), message.getUuid(),
                        MessageProcessStatusEnum.Success);

            } catch (Exception e) {
                logger.error("callback writeSuccessResult fail.", e);
            }
            logger.info("----------------------- callback write success message end ----------------");
        });
    }

    /**
     * 回写失败结果
     * 
     * @param message
     * @param callbackConfig
     * @param response
     * @param duration
     * @param throwable
     */
    public void writeFailResult(Message message, MessageCompensate messageCompensate, CallbackConfig callbackConfig,
            String response, long duration, Throwable throwable) {
        String requestId = MDC.get("logPrefix");

        taskExecutor.execute(() -> {
            MDC.put("logPrefix", requestId);

            logger.info("----------------------- callback write fail message begin ----------------");
            try {
                MessageStatus messageStatus = MessageStatus.from(message, callbackConfig);
                Boolean isFromDispatch = (messageCompensate == null); // 判断回调请求是否来自分发站

                if (isFromDispatch) {
                    messageStatus.setSource(MessageStatusSourceEnum.Dispatch.toString());
                } else {
                    messageStatus.setSource(MessageStatusSourceEnum.Compensate.toString());
                }
                messageStatus.setStatus(MessageStatusEnum.PushFail.toString());
                messageStatus.setFailResult(callbackConfig.getCallbackKey(), throwable, duration, response,
                        callbackConfig.getUrl());
                messageStatusRepository.insert(messageStatus, message.getAppId());

                if (isFromDispatch) {
                    if (callbackConfig.getIsRetry() == null || callbackConfig.getIsRetry().intValue() > 0) {
                        MessageCompensate compensate =
                                MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Dispatch);
                        messageCompensateRepository.insert(compensate);
                    }
                } else {
                    messageCompensate.incRetryCount();
                    if (new Date().after(messageCompensate.getRetryTimeout())) {
                        messageCompensate.setNewStatus(MessageCompensateStatusEnum.RetryFail.code());
                    } else {
                        messageCompensate.setNewStatus(MessageCompensateStatusEnum.Retrying.code());
                    }

                    messageCompensateRepository.update(messageCompensate);
                }

                if (isFromDispatch) {
                    if (callbackConfig.getIsRetry() == null || callbackConfig.getIsRetry().intValue() > 0) {
                        messageRepository.updateMessageStatusAndPublishTime(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageNewStatusEnum.DispatchToCompensate,
                                MessageProcessStatusEnum.Compensate);
                    } else {
                        messageRepository.updateMessageStatusAndPublishTime(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageNewStatusEnum.InRabbitMQ,
                                MessageProcessStatusEnum.Fail);
                    }
                } else {
                    if (messageCompensate.getNewStatus() == MessageCompensateStatusEnum.RetryFail.code()) {
                        messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageProcessStatusEnum.Fail);
                    } else {
                        messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageProcessStatusEnum.Compensate);
                    }
                }
                sendErrorReport(message, callbackConfig, throwable);

            } catch (Exception e) {
                logger.error(String.format("write callback fail result fail, appcode:%s, messageid:%s",
                        message.getAppCode(), message.getUuid()), e);
            }
            logger.info("----------------------- callback write fail message end ----------------");
        });
    }



    /**
     * 发送回调错误报告
     * 
     * @param appId
     * @param code
     * @param callbackConfig
     * @param message
     * @param uuid
     * @param ex
     */
    private void sendErrorReport(Message message, CallbackConfig callbackConfig, Throwable ex) {
        String consumerId = callbackConfig.getCallbackKey();
        String callbackAppId = callbackConfig.getCallbackAppId();

        String title = String.format(
                "messagebus callback Exception, appid:%s, code:%s, consumerId:%s, url:%s, messageId:%s, uuid:%s",
                message.getAppId(), message.getCode(), consumerId, callbackConfig.getUrl(), message.getMessageId(),
                message.getUuid());
        logger.error(title, ex);

        if (!StringUtils.isEmpty(callbackAppId)) {
            logger.info("sendErrorReport subscribe appId:{}", callbackAppId);
            errorReportClient.report(title, ex, callbackAppId);
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
