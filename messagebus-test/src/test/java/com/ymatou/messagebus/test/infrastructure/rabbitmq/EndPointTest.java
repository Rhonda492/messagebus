/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.rabbitmq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPoint;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPointEnum;
import com.ymatou.messagebus.test.BaseTest;

public class EndPointTest extends BaseTest {

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    private Channel channel4;

    @Test
    @Ignore
    public void testConnectionRecovery()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        EndPoint endPoint =
                EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava", "hello");

        System.out.println("请在循环中关闭RabbitMQ观察重连的效果！");
        for (int i = 0; i < 1000; i++) {
            System.out.println("EndPoint status:" + endPoint.isHealth());
            Thread.sleep(1000 * 10);
        }
    }

    @Test
    public void testNewInstanceConsumer()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        EndPoint.clear();

        EndPoint endPoint1 = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");
        EndPoint endPoint2 = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");
        EndPoint endPoint3 = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_demo");

        assertEquals(true, endPoint1 == endPoint2);
        assertEquals(false, endPoint1 == endPoint3);
        assertEquals(2, EndPoint.getEndPointMap().size());

        // Thread.sleep(1000 * 20);
    }

    @Test
    public void testNewInstanceProducer()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        EndPoint.clear();

        EndPoint endPoint1 = EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");
        EndPoint endPoint2 = EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");
        EndPoint endPoint3 = EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_demo");


        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    EndPoint endPoint4 =
                            EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getPrimaryUri(), "testjava",
                                    "testjava_hello");
                    channel4 = endPoint4.getChannel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();

        assertEquals(true, endPoint1 == endPoint2);
        assertEquals(false, endPoint1 == endPoint3);
        assertEquals(2, EndPoint.getEndPointMap().size());

        assertEquals(true, endPoint1.getChannel() == endPoint2.getChannel());
        assertEquals(true, endPoint1.getChannel() == endPoint3.getChannel());
        assertEquals(false, endPoint1.getChannel() == channel4);
    }
}
