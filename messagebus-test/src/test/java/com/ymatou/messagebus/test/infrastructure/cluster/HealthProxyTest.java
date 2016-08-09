/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.cluster;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.infrastructure.cluster.HealthProxy;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPoint;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPointEnum;
import com.ymatou.messagebus.test.BaseTest;

/**
 * 
 * @author wangxudong 2016年8月4日 下午2:22:20
 *
 */
public class HealthProxyTest extends BaseTest {

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Test
    public void testHealthProxy()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        EndPoint primary = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");
        EndPoint secondary = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), "testjava",
                "testjava_hello");

        HealthProxy healthProxy = new HealthProxy(primary, secondary);

        assertEquals(true, healthProxy.isHealth());
        assertEquals(true, healthProxy.primaryHealth());
        assertEquals(true, healthProxy.secondaryHealth());
    }
}
