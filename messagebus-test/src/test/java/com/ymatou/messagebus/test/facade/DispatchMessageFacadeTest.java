/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.CallbackInfo;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.facade.DispatchMessageFacade;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchMessageResp;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月12日 下午2:17:47
 *
 */
public class DispatchMessageFacadeTest extends BaseTest {

    @Resource
    private DispatchMessageFacade dispatchMessageFacade;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    private Message buildMessage(String code, String body) {
        Message message = new Message();
        message.setAppId("testjava");
        message.setCode(code);
        message.setNewStatus(1);
        message.setBody("hello");
        message.setMessageId(UUID.randomUUID().toString());
        message.setUuid(Message.newUuid());

        messageRepository.insert(message);
        messageRepository.updateMessageStatus(message.getAppId(), message.getCode(), message.getUuid(),
                MessageNewStatusEnum.CheckToCompensate, MessageProcessStatusEnum.Init);

        return message;
    }

    @Test
    public void testDispatch() throws InterruptedException {
        Message message = buildMessage("hello", "hello");

        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId(message.getAppId());
        req.setCode(message.getCode());
        req.setMessageBody(message.getBody());
        req.setMessageId(message.getMessageId());
        req.setMessageUuid(message.getUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(true, resp.isSuccess());

        Thread.sleep(100);

        MessageStatus messageStatus = messageStatusRepository.getByUuid(req.getAppId(), req.getMessageUuid());
        assertNotNull(messageStatus);
        assertEquals("RabbitMQ", messageStatus.getSource());
        assertEquals(req.getMessageUuid(), messageStatus.getMessageUuid());
        assertEquals(req.getMessageId(), messageStatus.getMessageId());
        assertEquals(1, messageStatus.getCallbackResult().size());
        assertEquals("PushOk", messageStatus.getStatus());

        MessageCompensate messageCompensate = messageCompensateRepository.getByUuid(message.getAppId(),
                message.getCode(), message.getUuid());
        assertNotNull(messageCompensate);
        assertEquals(2, messageCompensate.getStatus().intValue());
        assertEquals(message.getMessageId(), messageCompensate.getMessageId());
        assertEquals(message.getBody(), messageCompensate.getBody());
        assertEquals(0, messageCompensate.getNewStatus().intValue());
        assertEquals(2, messageCompensate.getSource().intValue());

        List<CallbackInfo> callbackList = messageCompensate.getCallbackList();
        assertEquals(1, callbackList.size());
        assertEquals(2, callbackList.get(0).getStatus().intValue());
        assertEquals(0, callbackList.get(0).getNewStatus().intValue());

        Message messageAssert =
                messageRepository.getByUuid(message.getAppId(), message.getCode(), message.getUuid());
        assertEquals(2, messageAssert.getNewStatus().intValue());
    }

    @Test
    public void testDispatch2Comsumer() throws InterruptedException {
        Message message = buildMessage("hello2", "hello");

        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId(message.getAppId());
        req.setCode(message.getCode());
        req.setMessageBody(message.getBody());
        req.setMessageId(message.getMessageId());
        req.setMessageUuid(message.getUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(true, resp.isSuccess());

        Thread.sleep(100);

        MessageStatus messageStatus = messageStatusRepository.getByUuid(req.getAppId(), req.getMessageUuid());
        assertNotNull(messageStatus);
        assertEquals("RabbitMQ", messageStatus.getSource());
        assertEquals(req.getMessageUuid(), messageStatus.getMessageUuid());
        assertEquals(req.getMessageId(), messageStatus.getMessageId());
        assertEquals(2, messageStatus.getCallbackResult().size());
        assertEquals("PushOk", messageStatus.getStatus());

        MessageCompensate messageCompensate = messageCompensateRepository.getByUuid(message.getAppId(),
                message.getCode(), message.getUuid());
        assertNotNull(messageCompensate);
        assertEquals(2, messageCompensate.getStatus().intValue());
        assertEquals(message.getMessageId(), messageCompensate.getMessageId());
        assertEquals(message.getBody(), messageCompensate.getBody());
        assertEquals(0, messageCompensate.getNewStatus().intValue());
        assertEquals(2, messageCompensate.getSource().intValue());

        List<CallbackInfo> callbackList = messageCompensate.getCallbackList();
        assertEquals(2, callbackList.size());
        assertEquals(2, callbackList.get(0).getStatus().intValue());
        assertEquals(0, callbackList.get(0).getNewStatus().intValue());

        Message messageAssert =
                messageRepository.getByUuid(message.getAppId(), message.getCode(), message.getUuid());
        assertEquals(2, messageAssert.getNewStatus().intValue());
    }

    @Test
    public void testDispatchFailWithInvalidAppId() {
        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId("testjava-x");
        req.setCode("hello");
        req.setMessageBody("hello");
        req.setMessageId(UUID.randomUUID().toString());
        req.setMessageUuid(Message.newUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("invalid appId"));
        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, resp.getErrorCode());
    }

    @Test
    public void testDispatchFailWithInvalidCode() {
        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId("testjava");
        req.setCode("hello-x");
        req.setMessageBody("hello");
        req.setMessageId(UUID.randomUUID().toString());
        req.setMessageUuid(Message.newUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("invalid appCode"));
        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, resp.getErrorCode());
    }

    @Test
    public void testDispatchFailWithEmptyMessageId() {
        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageBody("hello");
        req.setMessageUuid(Message.newUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("messageId can not be empty"));
        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, resp.getErrorCode());
    }

    @Test
    public void testDispatchFailWithEmptyMessageUuid() {
        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMessageBody("hello");
        req.setMessageId(UUID.randomUUID().toString());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("messageUuid can not be empty"));
        assertEquals(ErrorCode.ILLEGAL_ARGUMENT, resp.getErrorCode());
    }

    @Test
    public void testDispatchFailWithNotCallback() {
        DispatchMessageReq req = new DispatchMessageReq();
        req.setAppId("testjava");
        req.setCode("demo");
        req.setMessageBody("hello");
        req.setMessageId(UUID.randomUUID().toString());
        req.setMessageUuid(Message.newUuid());

        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);
        assertEquals(false, resp.isSuccess());
        assertEquals(ErrorCode.NOT_EXIST_INVALID_CALLBACK, resp.getErrorCode());
        assertEquals(true, resp.getErrorMessage().contains("不存在有效的回调信息"));
    }
}
