/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Test;

import com.ymatou.messagebus.client.MessageDB;
import com.ymatou.messagebus.client.MessageLocalConsumer;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.test.BaseTest;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

public class MessageLocalConsumerTest extends BaseTest {

    @Tested
    private MessageLocalConsumer messageLocalConsumer;

    @Injectable
    private MessageDB messageDB;

    @Injectable
    private PublishMessageFacade publishMessageFacade;

    private boolean invokeConsume() {
        try {
            Method consume = messageLocalConsumer.getClass().getDeclaredMethod("consume");
            consume.setAccessible(true);
            consume.invoke(messageLocalConsumer);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Test
    public void testConsume() {
        new Expectations() {
            {
                PublishMessageResp resp = new PublishMessageResp();
                resp.setSuccess(true);

                publishMessageFacade.publish(withAny(new PublishMessageReq()));
                result = resp;
                times = 1;

                messageDB.getKeysIterator("message");
                result = Arrays.asList("xxx").iterator();
                times = 1;

                messageDB.get("message", "xxx");
                result = new PublishMessageReq();
                times = 1;

                messageDB.delete("message", "xxx");
                result = new PublishMessageReq();
                times = 1;
            }
        };
        invokeConsume();
    }

    @Test
    public void testConsumeWhenIllegalArgument() {
        new Expectations() {
            {
                PublishMessageResp resp = new PublishMessageResp();
                resp.setSuccess(false);
                resp.setErrorCode(ErrorCode.ILLEGAL_ARGUMENT);

                publishMessageFacade.publish(withAny(new PublishMessageReq()));
                result = resp;
                times = 1;

                messageDB.getKeysIterator("message");
                result = Arrays.asList("xxx").iterator();
                times = 1;

                messageDB.get("message", "xxx");
                result = new PublishMessageReq();
                times = 1;

                messageDB.delete("message", "xxx");
                result = new PublishMessageReq();
                times = 1;
            }
        };
        invokeConsume();
    }

    @Test
    public void testConsumeWhenUnknow() {
        new Expectations() {
            {
                PublishMessageResp resp = new PublishMessageResp();
                resp.setSuccess(false);
                resp.setErrorCode(ErrorCode.UNKNOWN);

                publishMessageFacade.publish(withAny(new PublishMessageReq()));
                result = resp;
                times = 1;

                messageDB.getKeysIterator("message");
                result = Arrays.asList("xxx").iterator();
                times = 1;

                messageDB.get("message", "xxx");
                result = new PublishMessageReq();
                times = 1;
            }
        };
        invokeConsume();
    }

    @Test
    public void testConsumeWhenPublishException() {
        new Expectations() {
            {
                publishMessageFacade.publish(withAny(new PublishMessageReq()));
                result = new Exception("mock exception");
                times = 1;

                messageDB.getKeysIterator("message");
                result = Arrays.asList("xxx").iterator();
                times = 1;

                messageDB.get("message", "xxx");
                result = new PublishMessageReq();
                times = 1;
            }
        };
        invokeConsume();
    }
}
