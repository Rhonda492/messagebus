/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Connection;

/**
 * @author wangxudong 2016年8月3日 下午5:32:42
 *
 */
/**
 * @author wangxudong 2016年8月3日 下午5:32:47
 *
 */
public class ConnectionInfo {

    /**
     * RabbitMQ连接
     */
    private Connection connection;


    /**
     * 连接使用数
     */
    private AtomicInteger count = new AtomicInteger(0);

    public ConnectionInfo(Connection connection) {
        this.connection = connection;
    }


    /**
     * 获取连接
     * 
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 获取连接使用数
     * 
     * @return
     */
    public int getCount() {
        return count.intValue();
    }

    /**
     * 增加使用计数
     */
    public int incCount() {
        return count.incrementAndGet();
    }
}
