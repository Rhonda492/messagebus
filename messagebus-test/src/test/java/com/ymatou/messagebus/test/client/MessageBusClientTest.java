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

import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusClient;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

public class MessageBusClientTest extends BaseTest {

    @Resource
    private MessageBusClient messageBusClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSendMessageSuccess() throws MessageBusException {
        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageId("xxx-99");
        req.setBody(TaskItemRequest.newInstance());

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageBodyNull() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("body must not null");

        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageId("xxx-66");
        // req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageAppIdNull() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid appId");

        Message req = new Message();
        // req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageId("xxx-66");
        req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageCodeNull() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid code");

        Message req = new Message();
        req.setAppId("testjava");
        // req.setCode("hello");
        req.setMessageId("xxx-66");
        req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageMessageIdNull() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid message id");

        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello");
        // req.setMsgUniqueId("xxx-66");
        req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageInvalidAppId() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid appId");

        Message req = new Message();
        req.setAppId("testjava-x");
        req.setCode("hello");
        req.setMessageId("xxx-66");
        req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }


    @Test
    public void testSendMessageInvalidCode() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid code");

        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello-x");
        req.setMessageId("xxx-66");
        req.setBody("hello");

        messageBusClient.sendMessasge(req);
    }

}
