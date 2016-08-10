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
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AlarmRepository;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
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
    private ErrorReportClient errorReportClient;

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService#invoke(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void invoke(String exchange, String queue, String message, String messageId) {
        AppConfig appConfig = appConfigRepository.getAppConfig(exchange);
        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(queue);
        List<CallbackConfig> callbackCfgList = messageConfig.getCallbackCfgList();

        String appId = appConfig.getAppId();
        String code = messageConfig.getCode();

        for (CallbackConfig callbackConfig : callbackCfgList) {
            if (callbackConfig.getEnable() == null || callbackConfig.getEnable() == true) {
                String contentType = callbackConfig.getContentType();
                if (StringUtils.isEmpty(contentType)) {
                    contentType = "application/json;charset=utf-8";
                } else {
                    contentType = String.format("%s;charset=utf-8", contentType);
                }

                int timeout = 5000;
                if (callbackConfig.getCallbackTimeOut() != null && callbackConfig.getCallbackTimeOut() > 0) {
                    timeout = callbackConfig.getCallbackTimeOut().intValue();
                }

                try {
                    HttpResult result =
                            HttpClientUtil.sendPost(callbackConfig.getUrl(), message, contentType, null,
                                    httpClient, timeout);

                    logger.info("consumerId:{} callback http result:{}", callbackConfig.getCallbackKey(), result);

                    if (isCallbackSuccess(result)) {
                        messageRepository.updateMessageStatusAndPublishTime(appId, code, messageId,
                                MessageNewStatusEnum.Success.code());
                    } else {
                        publishToCompensate(appId, code, messageId, message, callbackConfig.getCallbackKey());
                        messageRepository.updateMessageStatusAndPublishTime(appId, code, messageId,
                                MessageNewStatusEnum.DispatchToCompensate.code());
                    }

                } catch (Exception e) {
                    sendErrorReport(appId, code, callbackConfig, messageId, e);
                }
            }
        }
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
     * @param ex
     */
    private void sendErrorReport(String appId, String code, CallbackConfig callbackConfig, String messageId,
            Throwable ex) {
        String consumerId = callbackConfig.getCallbackKey();
        Alarm alarm = alarmRepository.getByConsumerId(consumerId);

        String title = String.format(
                "messagebus callback Exception, appid:%s, code:%s, consumerId:%s, url:%s, messageId:%s",
                appId, code, consumerId, callbackConfig.getUrl(), messageId);
        logger.error(title, ex);
        errorReportClient.report(title, ex, alarm.getAlarmAppId());
    }

    /**
     * 发布消息到补偿库
     * 
     * @param appConfig
     * @param message
     */
    private void publishToCompensate(String appId, String code, String messageId, String body, String consumerId) {
        try {
            MessageCompensate messageCompensate =
                    MessageCompensate.newInstance(appId, code, messageId, body, consumerId);
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
