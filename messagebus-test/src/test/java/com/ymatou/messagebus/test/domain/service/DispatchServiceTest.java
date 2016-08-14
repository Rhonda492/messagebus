/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.service;

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
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

/**
 * @author wangxudong 2016年8月9日 下午2:14:55
 *
 */
public class DispatchServiceTest extends BaseTest {

    @Resource
    private PublishMessageFacade publishMessageFacade;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private DispatchService dispatchService;

    @Test
    public void testStart()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {

        String appId = "testjava";
        String code = "hello";
        PublishMessageResp resp = publishMessage(appId, code);
        dispatchService.start();

        Thread.sleep(300 * 1);

        assertEquals(true, MessageConsumer.getConsumerMap().size() > 0);


        Message message = messageRepository.getByUuid(appId, code, resp.getUuid());
        assertNotNull(message);
        assertEquals(MessageNewStatusEnum.InRabbitMQ.code(), message.getNewStatus());
        assertEquals(MessageProcessStatusEnum.Success.code(), message.getProcessStatus());
    }

    private PublishMessageResp publishMessage(String appId, String code) {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId(appId);
        req.setCode(code);
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

        return resp;
    }
}
