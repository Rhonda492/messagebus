/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.facade.enums.CallbackModeEnum;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;

import java.util.concurrent.TimeUnit;

/**
 * 调用业务系统
 * 
 * @author wangxudong 2016年8月16日 下午3:18:40
 *
 */
public class BizSystemCallback implements FutureCallback<HttpResponse> {

    private static Logger logger = LoggerFactory.getLogger(BizSystemCallback.class);

    public static final Integer CONN_TIME_OUT = 5000;
    public static final Integer SOCKET_TIME_OUT = 5000;
    public static final Integer CONN_REQ_TIME_OUT = 5000;

    private static RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(CONN_TIME_OUT)
            .setSocketTimeout(SOCKET_TIME_OUT)
            .setConnectionRequestTimeout(CONN_REQ_TIME_OUT)
            .build();

    private CloseableHttpAsyncClient httpClient;

    private HttpPost httpPost;

    private AdjustableSemaphore semaphore;

    private Message message;

    private MessageCompensate messageCompensate;

    private CallbackConfig callbackConfig;

    private CallbackServiceImpl callbackServiceImpl;

    private long beginTime;

    private boolean enableLog = true;

    /**
     * 回调模式
     */
    private CallbackModeEnum callbackMode;

    /**
     * 回调结果
     */
    private boolean callbackResult;

    /**
     * 构造异步回调实例
     * @param httpClient
     * @param url
     * @param body
     */
    public BizSystemCallback(CloseableHttpAsyncClient httpClient, Message message, MessageCompensate messageCompensate,
            CallbackConfig callbackConfig, CallbackServiceImpl callbackServiceImpl) {
        this.httpClient = httpClient;
        this.message = message;
        this.messageCompensate = messageCompensate;
        this.callbackConfig = callbackConfig;
        this.callbackServiceImpl = callbackServiceImpl;
        this.httpPost = new HttpPost(callbackConfig.getUrl().trim());
        this.semaphore = SemaphorManager.get(callbackConfig.getCallbackKey());

        setContentType(callbackConfig.getContentType());
        setTimeout(callbackConfig.getTimeout());

        this.callbackResult = false;
        if (messageCompensate == null) {
            this.callbackMode = CallbackModeEnum.Dispatch;
        } else {
            this.callbackMode = CallbackModeEnum.Compensate;
        }

    }


    /**
     * 设置超时
     * 
     * @param timeout
     * @return
     */
    public BizSystemCallback setTimeout(int timeout) {
        RequestConfig requestConfig = RequestConfig.copy(DEFAULT_REQUEST_CONFIG)
                // .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpPost.setConfig(requestConfig);

        return this;
    }

    /**
     * 设置是否写MongoDB日志
     * 
     * @param enableLog
     * @return
     */
    public BizSystemCallback setEnableLog(Boolean enableLog) {
        if (enableLog == null) {
            this.enableLog = true;
        } else {
            this.enableLog = enableLog;
        }

        return this;
    }

    public boolean isEnableLog() {
        return this.enableLog;
    }

    /**
     * 设置Content-Type
     * 
     * @param contentType
     * @return
     */
    public BizSystemCallback setContentType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        } else {
            httpPost.setHeader("Content-Type", String.format("%s;charset=utf-8", contentType));
        }

        return this;
    }

    /**
     * 判断回调是否成功
     * 
     * @param result
     * @return
     */
    private boolean isCallbackSuccess(int statusCode, String body) {
        if (statusCode == 200 && body != null
                && (body.trim().equalsIgnoreCase("ok") || body.trim().equalsIgnoreCase("\"ok\""))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送POST请求
     */
    public void send() throws InterruptedException {

        semaphore.acquire();

        String body = message.getBody();
        if (StringUtils.isEmpty(body) == false) {
            StringEntity postEntity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(postEntity);

            if (isEnableLog()) {
                logger.info("appcode:{}, messageUuid:{}, request body:{}.", message.getAppCode(), message.getUuid(),
                        body);
            }
        }
        beginTime = System.currentTimeMillis();
        httpClient.execute(httpPost, this);
    }

    /**
     * 秒级补单
     * 
     * @param timeSpanMS
     */
    public void secondCompensate(int timeSpanSecond) {
        this.callbackMode = CallbackModeEnum.SecondCompensate;
        for (int i = 0; i < 3; i++) {

            logger.info("secondCompensate no.{}, messageId:{}", i + 1, message.getMessageId());
            try {
                send();
            } catch (InterruptedException e) {
                logger.error(String.format("biz callback accquire semaphore fail, appcode:%s, messageUuid:%s",
                        message.getAppCode(), message.getUuid()), e);
            }

            try {
                Thread.sleep(timeSpanSecond * 1000);
            } catch (InterruptedException e) {
                logger.error("secondCompensate fail.", e);
            }

            if (callbackResult) {
                break;
            }
        }
        httpPost.releaseConnection();
    }

    /**
     * 释放资源
     */
    private void clear() {
        semaphore.release();
        if (this.callbackMode != CallbackModeEnum.SecondCompensate) {
            httpPost.releaseConnection();
        }
    }

    @Override
    public void completed(HttpResponse result) {
        try {
            HttpEntity entity = result.getEntity();
            String reponseStr = EntityUtils.toString(entity, "UTF-8");
            int statusCode = result.getStatusLine().getStatusCode();
            long duration = System.currentTimeMillis() - beginTime;

            logger.info("appcode:{}, messageUuid:{}, async response code:{}, duration:{}ms, message:{}.",
                    message.getAppCode(), message.getUuid(), statusCode, duration, reponseStr);
            //每个url回调性能监控
            PerformanceStatisticContainer.add(duration, String.format("%s_%s",callbackConfig.getCallbackKey(),callbackConfig.getUrl()),
                    CallbackServiceImpl.MONITOR_CALLBACK_KEY_URL_APP_ID);

            if (isCallbackSuccess(statusCode, reponseStr)) {
                callbackResult = true;
                callbackServiceImpl.writeSuccessResult(callbackMode, message, messageCompensate, callbackConfig,
                        duration, this.enableLog);
            } else {
                callbackServiceImpl.writeFailResult(callbackMode, message, messageCompensate, callbackConfig,
                        reponseStr, duration, null);
            }

        } catch (Exception e) {
            logger.error(String.format("appcode:%s, messageUuid:%s, %s completed.", message.getAppCode(),
                    message.getUuid(), httpPost.getRequestLine()), e);
        } finally {
            clear();
        }
    }

    @Override
    public void failed(Exception ex) {
        logger.error(String.format("appcode:%s, messageUuid:%s, %s failed.", message.getAppCode(),
                message.getUuid(), httpPost.getRequestLine()), ex);

        long duration = System.currentTimeMillis() - beginTime;
        callbackServiceImpl.writeFailResult(callbackMode, message, messageCompensate, callbackConfig, null, duration,
                ex);

        clear();

    }

    @Override
    public void cancelled() {
        logger.error("appcode:{}, messageUuid:{}, {} cancelled.", message.getAppCode(),
                message.getUuid(), httpPost.getRequestLine());

        long duration = System.currentTimeMillis() - beginTime;
        callbackServiceImpl.writeFailResult(callbackMode, message, messageCompensate, callbackConfig, "http cancelled",
                duration, null);

        clear();
    }
}
