/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.demo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import com.ymatou.messagebus.infrastructure.net.HttpClientUtil;

public class HttpAsyncClientDemo {

    public static void main(String[] args)
            throws UnsupportedEncodingException, IOReactorException, InterruptedException {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().setConnectionManager(cm).build();
        httpClient.start();

        System.out.println("main thread start send async post.");

        List<NameValuePair> body = new ArrayList<>();
        // params.add(new BasicNameValuePair("PaymentId", "15151254300985970"));
        // params.add(new BasicNameValuePair("TraceId", "xxxx-xxxx-111"));

        for (int i = 0; i < 100; i++) {
            HttpClientUtil.sendPost("http://www.baidu.com", body, null, httpClient);
        }
        System.out.println("wait for post");
        Thread.sleep(1000 * 10);
        System.out.println("end");
    }

}
