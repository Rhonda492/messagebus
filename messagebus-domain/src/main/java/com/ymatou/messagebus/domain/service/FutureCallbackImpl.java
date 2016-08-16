/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;

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

/**
 * 调用业务系统
 * 
 * @author wangxudong 2016年8月16日 下午3:18:40
 *
 */
public class FutureCallbackImpl implements FutureCallback<HttpResponse> {

    private static Logger logger = LoggerFactory.getLogger(FutureCallbackImpl.class);

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

    private Semaphore semaphore;

    /**
     * 构造异步回调实例
     * 
     * @param httpClient
     * @param url
     * @param body
     * @throws UnsupportedEncodingException
     */
    public FutureCallbackImpl(CloseableHttpAsyncClient httpClient, String url, Semaphore semaphore)
            throws UnsupportedEncodingException {
        this.httpClient = httpClient;
        this.httpPost = new HttpPost(url);
        this.semaphore = semaphore;

        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
    }

    /**
     * 设置超时
     * 
     * @param timeout
     * @return
     */
    public FutureCallbackImpl setTimeout(int timeout) {
        RequestConfig requestConfig = RequestConfig.copy(DEFAULT_REQUEST_CONFIG)
                // .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        httpPost.setConfig(requestConfig);

        return this;
    }

    /**
     * 设置Content-Type
     * 
     * @param contentType
     * @return
     */
    public FutureCallbackImpl setContentType(String contentType) {
        httpPost.setHeader("Content-Type", String.format("%s;charset=utf-8", contentType));

        return this;
    }

    /**
     * 发送POST请求
     */
    public void send(String body) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            logger.error("feature callback accquire semaphore fail.", e);
        }

        logger.info("executing request" + httpPost.getRequestLine());

        if (StringUtils.isEmpty(body) == false) {
            StringEntity postEntity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(postEntity);

            logger.info("request body: " + body);
        }

        httpClient.execute(httpPost, this);
    }

    private void clear() {
        semaphore.release();
        httpPost.releaseConnection();
    }

    @Override
    public void completed(HttpResponse result) {
        try {
            HttpEntity entity = result.getEntity();
            String reponseStr = EntityUtils.toString(entity, "UTF-8");
            logger.info("async response message:" + reponseStr);
        } catch (org.apache.http.ParseException e) {
            logger.error("async response message parse occur error.", e);
        } catch (IOException e) {
            logger.error("async response message read occur error.", e);
        } finally {
            clear();
        }
    }

    @Override
    public void failed(Exception ex) {
        logger.error(httpPost.getRequestLine() + " failed.", ex);
        clear();

    }

    @Override
    public void cancelled() {
        logger.error("{} cancelled.", httpPost.getRequestLine());
        clear();
    }

}
