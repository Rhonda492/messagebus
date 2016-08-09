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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.ymatou.messagebus.infrastructure.cluster.HealthService;

/**
 * @author wangxudong 2016年8月3日 下午4:09:14
 *
 */
public class EndPoint implements HealthService {

    private static Logger logger = LoggerFactory.getLogger(EndPoint.class);

    private ConnectionPool connectionPool;
    private Connection connection;
    private EndPointEnum endPointEnum;
    private String exchange;
    private String queue;
    private String uri;

    /**
     * 消费者Channel
     */
    private Channel consumerChannel;

    /**
     * 生产者Channel
     */
    private static ThreadLocal<Channel> producerChannel = new ThreadLocal<Channel>();

    /**
     * 终结点列表：key = {endPointEnum}_{uri}_{exchange}_{queue}
     */
    private static Map<String, EndPoint> endPointMap = new HashMap<String, EndPoint>();

    /**
     * 
     * 获取到终结点列表
     * 
     * @return
     */
    public static Map<String, EndPoint> getEndPointMap() {
        return endPointMap;
    }

    /**
     * 总结点构造器
     * 
     * @param uri
     * @param exchange
     * @param queue
     * @throws IOException
     * @throws TimeoutException
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    private EndPoint(EndPointEnum endPointEnum, String uri, String exchange, String queue)
            throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
        this.exchange = exchange;
        this.queue = queue;
        this.uri = uri;
        this.endPointEnum = endPointEnum;

        connectionPool = ConnectionPool.newInstance(uri);
        connectionPool.init();

        switch (endPointEnum) {
            case PRODUCER:
                getProducerChannel();
                break;
            case CONSUMER:
                initConsumerChannel();
                break;
        }
    }

    /**
     * 获取到终结点实例
     * 
     * @param uri
     * @param exchange
     * @param queue
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    public static EndPoint newInstance(EndPointEnum endPointEnum, String uri, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        String key = getEndPointId(endPointEnum, uri, exchange, queue);
        EndPoint endPoint = endPointMap.get(key);
        if (endPoint == null) {
            synchronized (endPointMap) {
                if (endPointMap.containsKey(key)) {
                    return endPointMap.get(key);
                } else {
                    endPoint = new EndPoint(endPointEnum, uri, exchange, queue);
                    endPointMap.put(key, endPoint);
                    return endPoint;
                }
            }
        } else {
            return endPoint;
        }
    }

    /**
     * 移除终结点
     * 
     * @param uri
     * @param exchange
     * @param queue
     */
    public static void remove(EndPoint endPoint) {
        String key = endPoint.getEndPointId();
        if (endPointMap.containsKey(key)) {
            synchronized (endPointMap) {
                endPointMap.remove(key);
            }
        }
    }

    /**
     * 清空终结点列表
     */
    public static void clear() {
        synchronized (endPointMap) {
            endPointMap.clear();
        }
    }

    /**
     * 获取终结点Id
     * 
     * @return
     */
    public String getEndPointId() {
        return getEndPointId(endPointEnum, uri, exchange, queue);
    }

    /**
     * 获取终结点Id
     * 
     * @param uri
     * @param exchange
     * @param queue
     * @return
     */
    public static String getEndPointId(EndPointEnum endPointEnum, String uri, String exchange, String queue) {
        return String.format("%s_%s_%s_%s", endPointEnum, uri, exchange, queue);

    }


    /**
     * 初始化消费者Channel
     * 
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    private void initConsumerChannel() throws IOException, TimeoutException {
        connection = connectionPool.getConnection();
        consumerChannel = connection.createChannel();

        consumerChannel.exchangeDeclare(exchange, "direct", true);
        consumerChannel.queueDeclare(queue, true, false, false, null);
        consumerChannel.queueBind(queue, exchange, queue);
    }

    /**
     * 获取到生产者Channel
     * 
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    private Channel getProducerChannel() throws IOException, TimeoutException {
        Channel channel = producerChannel.get();
        if (channel == null) {
            connection = connectionPool.getConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "direct", true);
            channel.queueDeclare(queue, true, false, false, null);
            producerChannel.set(channel);
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
    public void publish(Serializable object, BasicProperties basicProperties) throws IOException, TimeoutException {

        getProducerChannel().basicPublish(exchange, queue, basicProperties, SerializationUtils.serialize(object));
    }

    /**
     * 消费消息
     * 
     * @param consumer
     * @throws IOException
     */
    public void consume(Consumer consumer) throws IOException {
        consumerChannel.basicConsume(queue, true, consumer);
    }

    /**
     * 获取到Channel
     * 
     * @return
     * @throws Exception
     */
    public Channel getChannel() {
        try {
            switch (endPointEnum) {
                case PRODUCER:
                    return getProducerChannel();
                case CONSUMER:
                    return consumerChannel;
                default:
                    return null;
            }

        } catch (Exception e) {
            logger.error("endPoint get channel failed.", e);
            return null;
        }
    }

    /**
     * 判断终结点是否健康
     * 
     * @return
     */
    @Override
    public boolean isHealth() {
        Channel channel = getChannel();

        if (channel != null && channel.isOpen()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭链接
     * 
     * @throws IOException
     * @throws TimeoutException
     */
    public void close() throws IOException, TimeoutException {
        // 生产者的Channel是线程共享的不需要关闭
        if (EndPointEnum.PRODUCER == endPointEnum) {
            return;
        }

        Channel channel = getChannel();
        if (channel != null && channel.isOpen()) {
            remove(this);
            channel.close();
            channel = null;
        }
    }
}
