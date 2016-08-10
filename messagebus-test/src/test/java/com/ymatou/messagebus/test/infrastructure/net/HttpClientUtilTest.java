/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.net;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.ymatou.messagebus.infrastructure.net.HttpClientUtil;
import com.ymatou.messagebus.infrastructure.net.HttpResult;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

/**
 * @author wangxudong 2016年8月10日 下午3:33:37
 *
 */
public class HttpClientUtilTest extends BaseTest {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private CloseableHttpClient buildClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        return HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setConnectionManager(cm).build();
    }

    @Test
    public void testSendPost() throws IOException {
        CloseableHttpClient httpClient = buildClient();
        String url = "http://timedtask.ymatou.com:9999/api/CancelOrder/AddTaskItem";
        TaskItemRequest taskItemRequest = new TaskItemRequest();
        taskItemRequest.setId("9999");
        taskItemRequest.setTaskName("Trading.CancelOrder");
        taskItemRequest.setTaskTime(new Date());
        String jsonbody = JSON.toJSONStringWithDateFormat(taskItemRequest, DATE_FORMAT);

        HttpResult result =
                HttpClientUtil.sendPost(url, jsonbody, "application/json;charset=utf-8", null, httpClient, 1000 * 10);

        assertEquals(200, result.getStatusCode());
        assertEquals("\"ok\"", result.getBody());

    }

}
