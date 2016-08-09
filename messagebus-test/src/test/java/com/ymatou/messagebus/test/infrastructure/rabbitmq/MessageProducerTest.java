/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.rabbitmq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageProducer;
import com.ymatou.messagebus.infrastructure.rabbitmq.RabbitMQPublishException;
import com.ymatou.messagebus.test.BaseTest;

public class MessageProducerTest extends BaseTest {

    @Autowired
    RabbitMQConfig rabbitMQConfig;

    @Test
    public void testPublishNewInstance()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException, RabbitMQPublishException {
        MessageProducer messageProducer1 = MessageProducer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        MessageProducer messageProducer2 = MessageProducer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        MessageProducer messageProducer3 = MessageProducer.newInstance(rabbitMQConfig, "testjava", "testjava_demo");

        assertEquals(true, messageProducer1 == messageProducer2);
        assertEquals(false, messageProducer1 == messageProducer3);

        assertEquals(2, MessageProducer.getProducerMap().size());
    }

    @Test
    public void testIsHealth()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        MessageProducer messageProducer = MessageProducer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        boolean isHealth = messageProducer.isHealth();

        assertEquals(true, isHealth);
    }

    @Test
    public void testPublish()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            RabbitMQPublishException {
        MessageProducer messageProducer = MessageProducer.newInstance(rabbitMQConfig, "testjava", "testjava_hello");
        messageProducer.publishMessage("hello", "xxx-1");
    }
}
