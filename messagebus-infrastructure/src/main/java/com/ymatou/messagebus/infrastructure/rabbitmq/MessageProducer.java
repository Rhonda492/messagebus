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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.ymatou.messagebus.infrastructure.cluster.PSHealthProxy;
import com.ymatou.messagebus.infrastructure.cluster.HealthService;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;

/**
 * FIXME:关于MessageProducer和EndPoint的封装与缓存太复杂
 * MessageProducer应该是单例，由它负责出channel发消息即可
 * 消息生产者
 * 
 * @author wangxudong 2016年7月29日 下午2:50:14
 *
 */
public class MessageProducer implements HealthService {
    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    private EndPoint primary;
    private EndPoint secondary;
    private PSHealthProxy healthProxy;

    private boolean usePrimary = true;
    private boolean broken = false;
    private String exchange;
    private String queue;

    // key: exchange_queue
    private static Map<String, MessageProducer> messageProducerMap = new HashMap<String, MessageProducer>();

    /**
     * 获取到消息生产者列表
     * 
     * @return
     */
    public static Map<String, MessageProducer> getProducerMap() {
        return messageProducerMap;
    }

    /**
     * 消息生产者
     * 
     * @param rabbitMQConfig
     * @param exchange
     * @param queue
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    protected MessageProducer(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        this.exchange = exchange;
        this.queue = queue;
        this.primary = EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getPrimaryUri(), exchange, queue);
        this.secondary = EndPoint.newInstance(EndPointEnum.PRODUCER, rabbitMQConfig.getSecondaryUri(), exchange, queue);
        this.healthProxy = new PSHealthProxy(primary, secondary);
    }

    /**
     * 构造生产者实例
     * 
     * @param rabbitMQConfig
     * @param exchange
     * @param queue
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    public static MessageProducer newInstance(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        String key = String.format("%s_%s", exchange, queue);
        MessageProducer producer = messageProducerMap.get(key);
        if (producer == null) {
            synchronized (messageProducerMap) {
                if (messageProducerMap.containsKey(key)) {
                    return messageProducerMap.get(key);
                } else {
                    producer = new MessageProducer(rabbitMQConfig, exchange, queue);
                    messageProducerMap.put(key, producer);
                    return producer;
                }
            }
        } else {
            return producer;
        }
    }


    /**
     * 消息发布（主）
     * 
     * @param object
     * @param messageId
     * @param correlationId
     * @throws RabbitMQPublishException
     */
    public void publishMessage(Serializable object, String messageId, String correlationId)
            throws RabbitMQPublishException {
        setBroken(false);


        if (healthProxy.primaryHealth()) {
            try {

                BasicProperties basicProperties = new BasicProperties.Builder()
                        .messageId(messageId).correlationId(correlationId)
                        .type("primary")
                        .build();

                primary.publish(object, basicProperties);
                usePrimary = true;
            } catch (Exception e) {
                logger.error("primary publish failed:" + e.getMessage(), e);

                BasicProperties basicProperties = new BasicProperties.Builder()
                        .messageId(messageId).correlationId(correlationId)
                        .type("secondary")
                        .build();
                publishMessageSecondary(object, basicProperties);
            }
        } else if (healthProxy.secondaryHealth()) {
            BasicProperties basicProperties = new BasicProperties.Builder()
                    .messageId(messageId).correlationId(correlationId)
                    .type("secondary")
                    .build();
            publishMessageSecondary(object, basicProperties);
        } else {
            throw new RabbitMQPublishException();
        }
    }

    /**
     * 发布消息（备）
     * 
     * @param object
     * @throws RabbitMQPublishException
     */
    public void publishMessageSecondary(Serializable object, BasicProperties basicProperties)
            throws RabbitMQPublishException {
        if (usePrimary) {
            logger.error("change to secondary rabbitmq exchange:{}, queue:{}.", exchange, queue);
            usePrimary = false;
        }

        try {
            secondary.publish(object, basicProperties);
        } catch (Exception e) {
            logger.error("secondary publish failed:" + e.getMessage(), e);
            throw new RabbitMQPublishException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.cluster.HealthService#isHealth()
     */
    @Override
    public boolean isHealth() {
        return healthProxy.isHealth();
    }

    /**
     * @return the broken
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * @param broken the broken to set
     */
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
