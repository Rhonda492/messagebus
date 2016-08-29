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

import org.junit.Test;

import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPoint;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月4日 下午5:42:08
 *
 */
public class MessageConsumerTest extends BaseTest {

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Test
    public void testMessageConsumer()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        MessageConsumer consumer1 = MessageConsumer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        // consumer1.run();

        Thread thread = new Thread(consumer1);
        thread.start();
    }

    @Test
    public void testNewInstance()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        MessageConsumer consumer1 = MessageConsumer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        MessageConsumer consumer2 = MessageConsumer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        MessageConsumer consumer3 = MessageConsumer.newInstance(rabbitMQConfig, "testjava", "testjava_demo");

        assertEquals(true, consumer1 == consumer2);
        assertEquals(false, consumer1 == consumer3);

        assertEquals(2, MessageConsumer.getConsumerMap().size());
    }

    @Test
    public void testStop()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        MessageConsumer.clearAll();
        EndPoint.clear();

        MessageConsumer consumer = MessageConsumer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        assertEquals(1, MessageConsumer.getConsumerMap().size());
        assertEquals(2, EndPoint.getEndPointMap().size());

        MessageConsumer consumer1 = MessageConsumer.getConsumer("testjava", "testjava_hello");
        assertEquals(true, consumer == consumer1);

        consumer.stop();
        assertEquals(0, MessageConsumer.getConsumerMap().size());
        assertEquals(0, EndPoint.getEndPointMap().size());
    }
}
