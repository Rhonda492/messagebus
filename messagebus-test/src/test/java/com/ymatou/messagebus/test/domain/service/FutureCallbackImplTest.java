/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.Ignore;
import org.junit.Test;

import com.ymatou.messagebus.domain.service.FutureCallbackImpl;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月16日 下午4:05:03
 *
 */
public class FutureCallbackImplTest extends BaseTest {

    CloseableHttpAsyncClient httpClient;

    public FutureCallbackImplTest() throws IOReactorException {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        httpClient = HttpAsyncClients.custom().setConnectionManager(cm).build();
        httpClient.start();
    }

    @Test
    @Ignore
    public void testSend() throws UnsupportedEncodingException, InterruptedException {
        Semaphore semaphore = new Semaphore(10);

        String url = "http://mockforpay.iapi.ymatou.com/api/messagebus/delay/5000";
        for (int i = 0; i < 10; i++) {
            new FutureCallbackImpl(httpClient, url, semaphore).setTimeout(7000).send(null);
        }

        Thread.sleep(1000 * 60);
    }
}
