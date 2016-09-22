/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusClient;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.client.MessageDB;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

public class MessageBusClientTest extends BaseTest {

    private Logger logger = LoggerFactory.getLogger(MessageBusClientTest.class);

    @Resource
    private MessageBusClient messageBusClient;

    @Resource
    private MessageRepository messageRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");

    @Test
    public void testSendMessageSuccess() throws MessageBusException {
        Message req = new Message();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageId("xxx-200");
        req.setBody(TaskItemRequest.newInstance());

        logger.info(
                "---------------------------------------------client send message start-----------------------------------------------------");
        messageBusClient.sendMessage(req);
        logger.info(
                "---------------------------------------------client send message end-----------------------------------------------------");
    }

    @Test
    public void testSendMessageSuccessWithConstructor() throws MessageBusException {
        Message message = new Message("testjava", "hello", "xxx-300", TaskItemRequest.newInstance());

        messageBusClient.sendMessage(message);
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

        messageBusClient.sendMessage(req);
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

        messageBusClient.sendMessage(req);
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

        messageBusClient.sendMessage(req);
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

        messageBusClient.sendMessage(req);
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

        messageBusClient.sendMessage(req);
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

        messageBusClient.sendMessage(req);
    }

    @Test
    public void testPublishLocal() throws MessageBusException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello-x");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody("hello");

        MessageDB messageDB = invokeGetMessageDB();

        int beforeNum = messageDB.count("message");
        invokePublishLocal(req);

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
    public void testRetryThread() throws InterruptedException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody("hello");

        MessageDB messageDB = invokeGetMessageDB();
        messageDB.clear("message");

        int beforeNum = messageDB.count("message");
        assertEquals(0, beforeNum);

        invokePublishLocal(req);

        int afterNum = messageDB.count("message");
        assertEquals(1, afterNum);

        String key = req.getAppId() + req.getCode() + req.getMsgUniqueId();
        PublishMessageReq req2 = messageDB.get("message", key);
        assertNotNull(req2);
        assertEquals(req.getAppId(), req2.getAppId());
        assertEquals(req.getCode(), req2.getCode());
        assertEquals(req.getIp(), req2.getIp());
        assertEquals(req.getBody(), req2.getBody());

        // 给后台补发线程足够的处理时间
        Thread.sleep(1000 * 7);

        com.ymatou.messagebus.domain.model.Message message =
                messageRepository.getByMessageId(req.getAppId(), req.getCode(), simpleDateFormat.format(new Date()),
                        req.getMsgUniqueId());
        assertNotNull(message);
        assertEquals(req.getMsgUniqueId(), message.getMessageId());
        assertEquals(req.getIp(), message.getIp());
        assertEquals(req.getAppId(), message.getAppId());
        assertEquals(req.getAppId() + "_" + req.getCode(), message.getAppCode());

        System.out.println("messageId:" + message.getMessageId());

    }

    private boolean invokePublishLocal(PublishMessageReq req) {
        try {
            Method publishLocal =
                    messageBusClient.getClass().getDeclaredMethod("publishLocal", PublishMessageReq.class);
            publishLocal.setAccessible(true);
            publishLocal.invoke(messageBusClient, req);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Test
    public void testClearDB() {
        MessageDB messageDB = invokeGetMessageDB();

        int beforeNum = messageDB.count("message");
        assertEquals(true, beforeNum >= 0);

        messageDB.clear("message");
        int afterNum = messageDB.count("message");

        assertEquals(0, afterNum);
    }

    @Test
    public void testConsume() {
        MessageDB messageDB = invokeGetMessageDB();
        messageDB.clear("message");
        int afterNum = messageDB.count("message");
        assertEquals(0, afterNum);
    }

    private MessageDB invokeGetMessageDB() {
        try {
            Method getMessageDB =
                    messageBusClient.getClass().getDeclaredMethod("getMessageDB");
            getMessageDB.setAccessible(true);
            MessageDB result = (MessageDB) getMessageDB.invoke(messageBusClient);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("invoke getMessageDB faile", e);
        }
    }
}
