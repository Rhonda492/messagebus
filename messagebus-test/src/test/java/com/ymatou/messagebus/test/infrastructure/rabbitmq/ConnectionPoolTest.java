/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.rabbitmq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.ConnectionInfo;
import com.ymatou.messagebus.infrastructure.rabbitmq.ConnectionPool;
import com.ymatou.messagebus.test.BaseTest;

/**
 * 
 * @author wangxudong 2016年8月3日 下午6:37:54
 *
 */
public class ConnectionPoolTest extends BaseTest {

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Test
    public void testInitConn()
            throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException,
            InterruptedException {
        ConnectionPool.clearAll();

        ConnectionPool poolPrimary = ConnectionPool.newInstance(rabbitMQConfig.getPrimaryUri());
        poolPrimary.setInitConnNum(2);
        poolPrimary.setMaxChannelNumPerConn(5);
        poolPrimary.init();

        for (int i = 0; i < 20; i++) {
            Connection connection = poolPrimary.getConnection();
            connection.createChannel();
        }

        for (ConnectionInfo connectionInfo : poolPrimary.getConnList()) {
            assertEquals(5, connectionInfo.getCount());
        }

        ConnectionPool poolSlave = ConnectionPool.newInstance(rabbitMQConfig.getSecondaryUri());
        poolSlave.init();

        assertEquals(3, poolSlave.getConnList().size());

        for (int i = 0; i < 120; i++) {
            Connection connection = poolSlave.getConnection();
            connection.createChannel();
        }

        assertEquals(4, poolSlave.getConnList().size());

        for (ConnectionInfo connectionInfo : poolSlave.getConnList()) {
            assertEquals(30, connectionInfo.getCount());
        }
    }
}
