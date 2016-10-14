/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import javax.annotation.Resource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.client.KafkaBusClient;
import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

public class KafkaBusClientTest extends BaseTest {

    private Logger logger = LoggerFactory.getLogger(KafkaBusClientTest.class);

    @Resource
    private KafkaBusClient kafkaBusClient;


    @Test
    public void testSendMessageSuccess() throws MessageBusException {
        Message req = new Message();
        req.setAppId("testjava_kafka");
        req.setCode("hello");
        req.setMessageId("xxx-200");
        req.setBody(TaskItemRequest.newInstance());

        logger.info(
                "---------------------------------------------client send message start-----------------------------------------------------");
        kafkaBusClient.sendMessage(req);
        logger.info(
                "---------------------------------------------client send message end-----------------------------------------------------");
    }

    @Test
    public void testSendMessageAsyncSuccess() throws MessageBusException, InterruptedException {
        Message req = new Message();
        req.setAppId("testjava_kafka");
        req.setCode("hello");
        req.setMessageId("xxx-300");
        req.setBody(TaskItemRequest.newInstance());

        logger.info(
                "---------------------------------------------client send message start-----------------------------------------------------");
        kafkaBusClient.sendMessageAsync(req);
        logger.info(
                "---------------------------------------------client send message end-----------------------------------------------------");

        Thread.sleep(500);
    }
}
