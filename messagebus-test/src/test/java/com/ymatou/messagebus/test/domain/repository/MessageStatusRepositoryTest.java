/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import java.util.Date;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月11日 下午2:06:22
 *
 */
public class MessageStatusRepositoryTest extends BaseTest {

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Test
    public void testInsert() {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setId(ObjectId.get().toString());
        messageStatus.setMessageId("xxx-mid");
        messageStatus.setMessageUuid("xxx-uuid");
        messageStatus.setCreateTime(new Date());
        messageStatus.setSource("rabbitmq");
        messageStatus.setStatus("PushOK");
        messageStatus.setProccessIp(NetUtil.getHostIp());
        messageStatus.setSuccessResult("testjava_hello_c1", 300, "http://www.baidu.com");

        messageStatusRepository.insert(messageStatus, "testjava");
    }
}
