/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ymatou.messagebus.client.MessageBusClient;
import com.ymatou.messagebus.facade.model.PublishMessageReq;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/consumerApplicationContext.xml"})
public class MessageBusClientTest {

    @Resource
    private MessageBusClient messageBusClient;

    @Test
    public void testSendMessage() {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("xx");
        req.setCode("xx");
        req.setBody("hello");
        boolean resp = messageBusClient.sendMessasge(req);

        assertEquals(false, resp);
    }
}
