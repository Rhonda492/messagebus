/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;

/**
 * 消息消费者
 * 
 * @author wangxudong 2016年7月29日 下午3:34:32
 *
 */
public class MessageConsumer implements Runnable, Consumer {

    private static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private EndPoint primary;
    private EndPoint secondary;

    /**
     * 标识主消息队列是否健康
     */
    private boolean primaryHealth = true;
    /**
     * 标识备用消息队列是否健康
     */
    private boolean secondaryHealth = true;

    /**
     * 消费者构造函数
     * 
     * @param rabbitMQConfig
     * @param exchange
     * @param queue
     * @throws IOException
     * @throws TimeoutException
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     */
    public MessageConsumer(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
        primary = new EndPoint(rabbitMQConfig.getPrimaryUri(), exchange, queue);
        secondary = new EndPoint(rabbitMQConfig.getSecondaryUri(), exchange, queue);
    }


    @Override
    public void run() {
        try {
            primary.consume(this);
            secondary.consume(this);
        } catch (IOException e) {
            logger.error("consumer run failed.", e);
        }
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope env, BasicProperties props, byte[] body)
            throws IOException {
        String message = (String) SerializationUtils.deserialize(body);
        logger.info(message + " received.");

    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        logger.info("Consumer " + consumerTag + " registered");

    }

    @Override
    public void handleCancelOk(String consumerTag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        // TODO Auto-generated method stub

    }
}
