/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.model.Message;
import com.ymatou.messagebus.repository.MessageRepository;
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

        messageRepository.insert(message);
    }
}
