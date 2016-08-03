/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.SerializationUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

/**
 * @author wangxudong 2016年7月29日 下午2:28:21
 *
 */
/**
 * @author wangxudong 2016年7月29日 下午3:31:15
 *
 */
public class EndPoint {
    private ConnectionFactory factory = new ConnectionFactory();
    private Channel channel;
    private Connection connection;
    private String exchange;
    private String queue;

    public EndPoint(String uri, String exchange, String queue)
            throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
        this.exchange = exchange;
        this.queue = queue;

        factory.setUri(uri);
        channel = createChannel();
    }


    /**
     * 创建Channel
     * 
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    private Channel createChannel() throws IOException, TimeoutException {
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, queue);

        return channel;
    }

    /**
     * 获取到Channel
     * 
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    private Channel getChannel() throws IOException, TimeoutException {
        if (channel == null) {
            channel = createChannel();
        }
        return channel;
    }

    /**
     * 发布消息
     * 
     * @param object
     * @throws IOException
     * @throws TimeoutException
     */
    public void publish(Serializable object) throws IOException, TimeoutException {
        getChannel().basicPublish(exchange, queue, null, SerializationUtils.serialize(object));
    }

    /**
     * 消费消息
     * 
     * @param consumer
     * @throws IOException
     */
    public void consume(Consumer consumer) throws IOException {
        channel.basicConsume(queue, true, consumer);
    }

    /**
     * 关闭链接
     * 
     * @throws IOException
     * @throws TimeoutException
     */
    public void close() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
    }
}
