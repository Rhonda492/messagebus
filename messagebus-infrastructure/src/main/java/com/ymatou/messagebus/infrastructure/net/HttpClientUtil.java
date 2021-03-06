/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.json.ParseException;

/**
 * 用于提交get,post请求
 * 
 * @author qianmin 2016年5月9日 上午10:42:18
 *
 */
public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static final Integer CONN_TIME_OUT = 5000;
    public static final Integer SOCKET_TIME_OUT = 5000;
    public static final Integer CONN_REQ_TIME_OUT = 5000;

    private static RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(CONN_TIME_OUT)
            .setSocketTimeout(SOCKET_TIME_OUT)
            .setConnectionRequestTimeout(CONN_REQ_TIME_OUT)
            .build();



    /**
     * 
     * @param url 请求路径
     * @param body 请求body
     * @param contentType 实体类型
     * @param header 请求header
     * @param httpClient 执行请求的HttpClient
     * @return 请求应答
     * @throws ParseException
     * @throws IOException
     */
    public static HttpResult sendPost(String url, String body, String contentType, HashMap<String, String> header,
            HttpClient httpClient, int timeout)
            throws IOException {
        HttpResult result = new HttpResult();

        RequestConfig requestConfig = RequestConfig.copy(DEFAULT_REQUEST_CONFIG)
                .setConnectionRequestTimeout(timeout).build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        StringEntity postEntity = new StringEntity(body, "UTF-8");
        httpPost.setEntity(postEntity);
        httpPost.addHeader("Content-Type", contentType);

        logger.info("executing request" + httpPost.getRequestLine());
        logger.info("request header: " + Arrays.toString(httpPost.getAllHeaders()));
        logger.info("request body: " + body);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String respBody = EntityUtils.toString(entity, "UTF-8");
            logger.info("response body:" + respBody);

            result.setBody(respBody);
            result.setStatusCode(response.getStatusLine().getStatusCode());

        } finally {
            httpPost.releaseConnection();
        }

        return result;
    }

    /**
     * 发送HTTP POST请求
     * 
     * @param url 请求路径
     * @param body 请求body
     * @param header 请求header
     * @param httpClient 执行请求的HttpClient
     * @return 请求应答
     * @throws UnsupportedEncodingException
     * @throws ParseException
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void sendPost(String url, List<NameValuePair> body, HashMap<String, String> header,
            CloseableHttpAsyncClient httpClient) throws UnsupportedEncodingException {

        HttpPost httpPost = new HttpPost(url);
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(body, "UTF-8");
        httpPost.setEntity(postEntity); // set request body
        logger.info("executing request" + httpPost.getRequestLine());
        logger.info("request header: " + Arrays.toString(httpPost.getAllHeaders()));
        logger.info("request body: " + body);

        httpClient.execute(httpPost, new FutureCallback<HttpResponse>() {

            @Override
            public void failed(Exception ex) {
                logger.error(httpPost.getRequestLine() + " failed.", ex);
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
                    httpPost.releaseConnection();
                }

            }

            @Override
            public void cancelled() {
                logger.error("{} cancelled.", httpPost.getRequestLine());
                httpPost.releaseConnection();
            }
        });
    }
}
