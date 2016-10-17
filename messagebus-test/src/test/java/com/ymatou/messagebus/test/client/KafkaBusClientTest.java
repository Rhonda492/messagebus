/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import javax.annotation.Resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ymatou.messagebus.client.KafkaBusClient;
import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

public class KafkaBusClientTest extends BaseTest {

    @Resource
    private KafkaBusClient kafkaBusClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSendMessageSuccess() throws MessageBusException {
        Message req = new Message();
        req.setAppId("testjava_kafka");
        req.setCode("hello");
        req.setMessageId("xxx-200");
        req.setBody(TaskItemRequest.newInstance());
        kafkaBusClient.sendMessage(req);
    }

    @Test
    public void testSendMessageBodyNull() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("body must not null");
        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageId("messageId");
        kafkaBusClient.sendMessage(req);
    }

    @Test
    public void testSendMessageAsyncSuccess() throws MessageBusException, InterruptedException {
        Message req = new Message();
        req.setAppId("testjava_kafka");
        req.setCode("hello");
        req.setMessageId("xxx-300");
        req.setBody(TaskItemRequest.newInstance());

        kafkaBusClient.sendMessageAsync(req);

        Thread.sleep(500);
    }
}
