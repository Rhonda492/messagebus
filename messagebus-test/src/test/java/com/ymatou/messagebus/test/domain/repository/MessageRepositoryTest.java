/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.test.BaseTest;

public class MessageRepositoryTest extends BaseTest {

    @Resource
    MessageRepository messageRepository;

    @Test
    public void testInsert() {
        Message message = new Message();
        message.setUuid(Message.newUuid());
        message.setMessageId(UUID.randomUUID().toString());
        message.setAppId("testjava");
        message.setCode("hello");
        message.setNewStatus(1);

        messageRepository.insert(message);
    }

    @Test
    public void testUpdate() {
        Message message = new Message();
        message.setAppId("testjava");
        message.setCode("hello");
        message.setNewStatus(1);
        message.setMessageId(UUID.randomUUID().toString());
        message.setUuid(Message.newUuid());

        messageRepository.insert(message);
        messageRepository.updateMessageStatus(message.getAppId(), message.getCode(), message.getUuid(),
                MessageNewStatusEnum.CheckToCompensate, MessageProcessStatusEnum.Init);

        Message messageAssert =
                messageRepository.getByUuid(message.getAppId(), message.getCode(), message.getUuid());

        assertEquals(MessageNewStatusEnum.CheckToCompensate.code().intValue(), messageAssert.getNewStatus().intValue());
    }
}
