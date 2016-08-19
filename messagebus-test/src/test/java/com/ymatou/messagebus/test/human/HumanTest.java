/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.human;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.service.DispatchService;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

/**
 * 人工测试
 * 
 * @author wangxudong 2016年8月9日 下午7:06:11
 *
 */
public class HumanTest extends BaseTest {

    @Resource
    private PublishMessageFacade publishMessageFacade;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private DispatchService dispatchService;



    @Test
    public void testPublishMessage() throws InterruptedException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setIp(NetUtil.getHostIp());


        for (int i = 0; i < 1; i++) {
            req.setMsgUniqueId(UUID.randomUUID().toString());
            req.setBody(TaskItemRequest.newInstance());

            PublishMessageResp resp = publishMessageFacade.publish(req);
            System.out.println(String.format("isSuccess:%s, message:%s", resp.isSuccess(), resp.getErrorMessage()));


            Thread.sleep(1000 * 10);
        }

        Thread.sleep(1000 * 30);
    }

    @Test
    public void testConsumerStart()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        testPublish();

        dispatchService.start();

        Thread.sleep(1000 * 100);
    }

    @Test
    public void testPublish() throws InterruptedException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setMsgUniqueId(UUID.randomUUID().toString());
        req.setBody(TaskItemRequest.newInstance());
        req.setIp(NetUtil.getHostIp());
        PublishMessageResp resp = publishMessageFacade.publish(req);

        System.out.println(resp.getErrorMessage());
        assertEquals(true, resp.isSuccess());

        Thread.sleep(500);

        Message message = messageRepository.getByUuid(req.getAppId(), req.getCode(), resp.getUuid());
        assertNotNull(message);
        assertEquals(req.getMsgUniqueId(), message.getMessageId());
        assertEquals(req.getIp(), message.getIp());
        assertEquals(req.getAppId(), message.getAppId());
        assertEquals(req.getAppId() + "_" + req.getCode(), message.getAppCode());
    }
}
