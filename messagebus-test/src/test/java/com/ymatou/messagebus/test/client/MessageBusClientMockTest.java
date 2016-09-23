/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import static mockit.Deencapsulation.setField;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusClient;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.client.MessageDB;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.test.BaseTest;

import mockit.Mock;
import mockit.MockUp;

public class MessageBusClientMockTest extends BaseTest {

    private MessageBusClient messageBusClient = new MessageBusClient();

    @Resource
    private PublishMessageFacade publishMessageFacade;

    private MessageDB messageDB;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    private void mockPublishMessageFacade(boolean result, ErrorCode errorCode, String errMessage) {
        publishMessageFacade = new MockUp<PublishMessageFacade>() {
            @Mock
            PublishMessageResp publish(PublishMessageReq req) {
                PublishMessageResp resp = new PublishMessageResp();
                resp.setSuccess(result);
                resp.setErrorCode(errorCode);
                resp.setErrorMessage(errMessage);

                return resp;
            }
        }.getMockInstance();
        setField(messageBusClient, publishMessageFacade);
    }

    private void mockPublishMessageFacadeException() {
        publishMessageFacade = new MockUp<PublishMessageFacade>() {
            @Mock
            PublishMessageResp publish(PublishMessageReq req) {
                throw new RuntimeException("mock exception");
            }
        }.getMockInstance();
        setField(messageBusClient, publishMessageFacade);
    }

    private void mockMessageDB(boolean saveException) {
        messageDB = new MockUp<MessageDB>() {
            @Mock
            public <T> void save(String mapName, String key, T value) {
                if (saveException) {
                    throw new RuntimeException("mock exception");
                }
            }
        }.getMockInstance();
        setField(messageBusClient, messageDB);
    }

    @Test
    public void testSendMessageWhenFacadeReturnIllegalArgument() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("invalid appId");

        mockPublishMessageFacade(false, ErrorCode.ILLEGAL_ARGUMENT, "invalid appId");

        Message message = new Message("testjava", "xxx", UUID.randomUUID().toString(), "mock");
        messageBusClient.sendMessage(message);
    }

    @Test
    public void testSendMessageWhenFacadeFailToLocal() throws MessageBusException {
        mockPublishMessageFacade(false, ErrorCode.UNKNOWN, "system error");
        mockMessageDB(false);

        Message message = new Message("testjava", "hello", UUID.randomUUID().toString(), "mock");
        messageBusClient.sendMessage(message);
    }

    @Test
    public void testSendMessageWhenFacadeFailToLocalException() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("publish message local fail");

        mockPublishMessageFacade(false, ErrorCode.UNKNOWN, "system error");
        mockMessageDB(true);

        Message message = new Message("testjava", "hello", UUID.randomUUID().toString(), "mock");
        messageBusClient.sendMessage(message);
    }

    @Test
    public void testSendMessageWhenFacadethrowException() throws MessageBusException {
        mockPublishMessageFacadeException();
        mockMessageDB(false);

        Message message = new Message("testjava", "hello", UUID.randomUUID().toString(), "mock");
        messageBusClient.sendMessage(message);
    }

    @Test
    public void testSendMessageWhenFacadethrowExceptionAndPublishLocalException() throws MessageBusException {
        thrown.expect(MessageBusException.class);
        thrown.expectMessage("publish message local fail");

        mockPublishMessageFacadeException();
        mockMessageDB(true);

        Message message = new Message("testjava", "hello", UUID.randomUUID().toString(), "mock");
        messageBusClient.sendMessage(message);
    }
}
