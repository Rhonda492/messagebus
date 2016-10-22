/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ymatou.messagebus.client.Message;
import com.ymatou.messagebus.client.MessageBusException;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

public class MessageTest extends BaseTest {

    @Test
    public void testValidateToReq() throws MessageBusException {
        Message message = new Message();
        message.setAppId("demo");
        message.setCode("hello");
        message.setMessageId("xxx");
        message.setBody(TaskItemRequest.newInstance());

        PublishMessageReq validateToReq = message.validateToReq(true);
        assertEquals(false, validateToReq.getBody().contains("taskType"));

        validateToReq = message.validateToReq(false);
        assertEquals(true, validateToReq.getBody().contains("taskType"));
    }
}
