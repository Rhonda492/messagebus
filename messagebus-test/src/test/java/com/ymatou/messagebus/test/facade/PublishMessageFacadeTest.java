/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.facade;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.MessageBody;

public class PublishMessageFacadeTest extends BaseTest {

    @Resource
    private PublishMessageFacade publishMessageFacade;

    @Test
    public void testPublish() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody(MessageBody.newInstance());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());

        assertEquals(true, resp.isSuccess());
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
}
