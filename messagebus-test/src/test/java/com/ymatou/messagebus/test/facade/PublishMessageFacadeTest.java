/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.MessageBody;
import com.ymatou.messagebus.test.TaskItemRequest;

public class PublishMessageFacadeTest extends BaseTest {

    @Resource
    private PublishMessageFacade publishMessageFacade;

    @Resource
    private MessageRepository messageRepository;

    @Test
    public void testPublish() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody(TaskItemRequest.newInstance());
        req.setIp(NetUtil.getHostIp());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());
        assertEquals(true, resp.isSuccess());

        Message message = messageRepository.getByUuid(req.getAppId(), req.getCode(), resp.getUuid());
        assertNotNull(message);
        assertEquals(req.getMsgUniqueId(), message.getMessageId());
        assertEquals(req.getIp(), message.getIp());
        assertEquals(req.getAppId(), message.getAppId());
        assertEquals(req.getAppId() + "_" + req.getCode(), message.getAppCode());
    }



    @Test
    public void testPublishAppidInvalid() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava1");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody(MessageBody.newInstance());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());

        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("invalid appId"));
    }

    @Test
    public void testPublishAppCodeInvalid() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello1");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody(MessageBody.newInstance());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());

        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("invalid code"));
    }

    @Test
    public void testPublishBodyNull() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        // req.setBody(MessageBody.newInstance());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());

        assertEquals(false, resp.isSuccess());
        assertEquals(true, resp.getErrorMessage().contains("body not null"));
    }
}
