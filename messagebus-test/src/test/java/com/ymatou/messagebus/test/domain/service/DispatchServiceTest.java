/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.service.DispatchService;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月9日 下午2:14:55
 *
 */
public class DispatchServiceTest extends BaseTest {

    @Resource
    private DispatchService dispatchService;

    @Test
    public void testStart()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException,
            InterruptedException {
        dispatchService.start();

        assertEquals(1, MessageConsumer.getConsumerMap().size());
    }
}
