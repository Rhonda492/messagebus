/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.test.BaseTest;

public class MessageRepositoryTest extends BaseTest {

    @Resource
    MessageRepository messageRepository;

    @Test
    public void testInsert() {
        Message message = new Message();
        message.setAppId("javatest");
        message.setCode("hello");
        message.setAppCode("javatest_hello");
        message.setNewStatus(1);

        messageRepository.insert(message);
    }

    @Test
    public void testUpdate() {
        Message message = new Message();
        message.setAppId("javatest");
        message.setCode("hello");
        message.setAppCode("javatest_hello");
        message.setNewStatus(1);
        message.setMessageId(UUID.randomUUID().toString());

        messageRepository.insert(message);
        messageRepository.updateMessageStatus(message.getAppId(), message.getCode(), message.getMessageId(), 3);

        Message messageAssert =
                messageRepository.getByMessageId(message.getAppId(), message.getCode(), message.getMessageId());

        assertEquals(3, messageAssert.getNewStatus().intValue());
    }
}
