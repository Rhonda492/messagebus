/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.service.CallbackServiceImpl;
import com.ymatou.messagebus.test.BaseTest;

public class CallbackServiceImplTest extends BaseTest {

    @Resource
    private CallbackServiceImpl callbackServiceImpl;

    @Test
    public void testInvoke() {
        callbackServiceImpl.invoke("testjava", "testjava_hello", "hello", "0b60d46b-09cf-45ba-950d-f14a09516d87");
    }
}
