/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusClient;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.client.MessageDB;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
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
        req.setMessageId("xxx-200");
        req.setBody(TaskItemRequest.newInstance());

        messageBusClient.sendMessasge(req);
    }

    @Test
    public void testSendMessageSuccessWithConstructor() throws MessageBusException {
        Message message = new Message("testjava", "hello", "xxx-300", TaskItemRequest.newInstance());

        messageBusClient.sendMessasge(message);
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

    @Test
    public void testPublishLocal() throws MessageBusException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello-x");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody("hello");

        MessageDB messageDB = messageBusClient.getMessageDB();

        int beforeNum = messageDB.count("message");
        messageBusClient.publishLocal(req);

        int afterNum = messageDB.count("message");
        assertEquals(beforeNum + 1, afterNum);

        String key = req.getAppId() + req.getCode() + req.getMsgUniqueId();
        PublishMessageReq req2 = messageDB.get("message", key);
        assertNotNull(req2);
        assertEquals(req.getAppId(), req2.getAppId());
        assertEquals(req.getCode(), req2.getCode());
        assertEquals(req.getIp(), req2.getIp());
        assertEquals(req.getBody(), req2.getBody());
    }

    @Test
    public void testClearDB() {
        MessageDB messageDB = messageBusClient.getMessageDB();

        int beforeNum = messageDB.count("message");
        assertEquals(true, beforeNum >= 0);

        messageDB.clear("message");
        int afterNum = messageDB.count("message");

        assertEquals(0, afterNum);
    }

    @Test
    public void testConsume() {
        MessageDB messageDB = messageBusClient.getMessageDB();
        messageDB.clear("message");
        int afterNum = messageDB.count("message");
        assertEquals(0, afterNum);



    }
}
