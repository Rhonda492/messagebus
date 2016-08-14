/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.Alarm;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.CallbackInfo;
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
import com.ymatou.messagebus.infrastructure.net.HttpClientUtil;
import com.ymatou.messagebus.infrastructure.net.HttpResult;
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

    private CloseableHttpClient httpClient;

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

        invokeCore(appConfig.getAppId(), messageConfig.getCode(), messageId, messageUuid, messageBody, callbackCfgList);
    }

    /**
     * 回调核心逻辑
     * 
     * @param appId
     * @param code
     * @param messageId
     * @param uuid
     * @param messageBody
     * @param callbackCfgList
     */
    private void invokeCore(String appId, String code, String messageId, String uuid, String messageBody,
            List<CallbackConfig> callbackCfgList) {
        List<CallbackInfo> callbackInfoList = new ArrayList<CallbackInfo>();
        MessageStatus messageStatus =
                new MessageStatus(uuid, messageId, MessageStatusEnum.PushOk, MessageStatusSourceEnum.RabbitMQ);

        for (CallbackConfig callbackConfig : callbackCfgList) {
            if (callbackConfig.getEnable() == null || callbackConfig.getEnable() == true) {

                if (!invokeBizSystem(messageStatus, callbackConfig, appId, code, uuid, messageId, messageBody)) {
                    CallbackInfo callbackInfo = new CallbackInfo();
                    callbackInfo.setCallbackKey(callbackConfig.getCallbackKey());
                    callbackInfo.setStatus(MessageCompensateStatusEnum.RetryOk.code());// 避免.NET补单
                    callbackInfo.setNewStatus(MessageCompensateStatusEnum.NotRetry.code());
                    callbackInfoList.add(callbackInfo);
                }
            }
        }

        if (callbackInfoList.size() == 0) {
            messageRepository.updateMessageStatusAndPublishTime(appId, code, uuid, MessageNewStatusEnum.InRabbitMQ,
                    MessageProcessStatusEnum.Success);
        } else {
            publishToCompensate(appId, code, uuid, messageId, messageBody, callbackInfoList);
            messageRepository.updateMessageStatusAndPublishTime(appId, code, uuid,
                    MessageNewStatusEnum.DispatchToCompensate, MessageProcessStatusEnum.Compensate);
        }

        messageStatusRepository.insert(messageStatus, appId);
    }

    /**
     * 回调业务系统
     * 
     * @param messageStatus
     * @param callbackConfig
     * @param appId
     * @param code
     * @param uuid
     * @param messageId
     * @param message
     * @return
     */
    public boolean invokeBizSystem(MessageStatus messageStatus, CallbackConfig callbackConfig, String appId,
            String code, String uuid, String messageId, String message) {
        String consumerId = callbackConfig.getCallbackKey();
        String callbackUrl = callbackConfig.getUrl();
        String contentType = getContentType(callbackConfig);
        int timeout = getTimeout(callbackConfig);

        long duration;
        long startMill = System.currentTimeMillis();
        boolean callbackStatus = false;
        try {
            HttpResult result =
                    HttpClientUtil.sendPost(callbackUrl, message, contentType, null, httpClient, timeout);
            duration = System.currentTimeMillis() - startMill;

            logger.info("consumerId:{} callback http, duration:{}ms, result:{}",
                    callbackConfig.getCallbackKey(), duration, result);

            callbackStatus = isCallbackSuccess(result);
            if (callbackStatus == true) {
                messageStatus.addSuccessResult(consumerId, duration, callbackUrl);
            } else {
                messageStatus.addFailResult(consumerId, null, duration, result.getBody(), callbackUrl);
            }

        } catch (Exception e) {
            duration = System.currentTimeMillis() - startMill;
            sendErrorReport(appId, code, callbackConfig, messageId, uuid, e);

            messageStatus.addFailResult(consumerId, e.getMessage(), duration, "", callbackUrl);
        }

        return callbackStatus;
    }

    /**
     * 从配置中获取ContentType
     * 
     * @param callbackConfig
     * @return
     */
    private String getContentType(CallbackConfig callbackConfig) {
        String contentType = callbackConfig.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            return "application/json;charset=utf-8";
        } else {
            return String.format("%s;charset=utf-8", contentType);
        }
    }

    /**
     * 从配置中获取Timeout
     * 
     * @param callbackConfig
     * @return
     */
    private int getTimeout(CallbackConfig callbackConfig) {
        int timeout = 5000;
        if (callbackConfig.getCallbackTimeOut() != null && callbackConfig.getCallbackTimeOut() > 0) {
            timeout = callbackConfig.getCallbackTimeOut().intValue();
        }
        return timeout;
    }

    /**
     * 判断回调是否成功
     * 
     * @param result
     * @return
     */
    private boolean isCallbackSuccess(HttpResult result) {
        if (result.getStatusCode() == 200 && result.getBody() != null
                && (result.getBody().equalsIgnoreCase("ok") || result.getBody().equalsIgnoreCase("\"ok\""))) {
            return true;
        } else {
            return false;
        }
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
    private void sendErrorReport(String appId, String code, CallbackConfig callbackConfig, String messageId,
            String uuid, Throwable ex) {
        String consumerId = callbackConfig.getCallbackKey();
        Alarm alarm = alarmRepository.getByConsumerId(consumerId);

        String title = String.format(
                "messagebus callback Exception, appid:%s, code:%s, consumerId:%s, url:%s, messageId:%s, uuid:%s",
                appId, code, consumerId, callbackConfig.getUrl(), messageId, uuid);
        logger.error(title, ex);
        errorReportClient.report(title, ex, alarm.getAlarmAppId());
    }

    /**
     * 发布消息到补偿库
     * 
     * @param appConfig
     * @param message
     */
    private void publishToCompensate(String appId, String code, String uuid, String messageId, String body,
            List<CallbackInfo> listCallbackInfo) {
        try {
            MessageCompensate messageCompensate =
                    MessageCompensate.newInstance(appId, code, uuid, messageId, body, listCallbackInfo);
            messageCompensate.setSource(MessageCompensateSourceEnum.Dispatch.code());
            messageCompensateRepository.insert(messageCompensate);
        } catch (Exception ex) {
            logger.error("publish to mongodb failed with appcode:" + appId + "_" + code, ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        httpClient =
                HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setConnectionManager(cm).build();
    }

}
