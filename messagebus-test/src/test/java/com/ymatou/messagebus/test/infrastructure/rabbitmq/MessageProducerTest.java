/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageProducer;
import com.ymatou.messagebus.infrastructure.rabbitmq.RabbitMQPublishException;
import com.ymatou.messagebus.test.BaseTest;

public class MessageProducerTest extends BaseTest {

    @Autowired
    RabbitMQConfig rabbitMQConfig;

    @Test
    public void publishMessageTest()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException, RabbitMQPublishException {

        MessageConsumer consumer = new MessageConsumer(rabbitMQConfig, "test", "test.simple_queue");
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        MessageProducer producer = MessageProducer.newInstance(rabbitMQConfig, "test", "test.simple_queue");
        for (int i = 0; i < 3; i++) {
            producer.publishMessage(String.valueOf(i));

            Thread.sleep(1000 * 3);
        }
    }
}
