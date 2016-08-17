/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.service;

import java.io.UnsupportedEncodingException;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.Ignore;
import org.junit.Test;

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

    }
}
