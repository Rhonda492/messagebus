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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;

/**
 * 消息生产者
 * 
 * @author wangxudong 2016年7月29日 下午2:50:14
 *
 */
public class MessageProducer {
    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

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

    protected MessageProducer(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        primary = new EndPoint(rabbitMQConfig.getPrimaryUri(), exchange, queue);
        secondary = new EndPoint(rabbitMQConfig.getSecondaryUri(), exchange, queue);
    }

    public static MessageProducer newInstance(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        return new MessageProducer(rabbitMQConfig, exchange, queue);
    }

    /**
     * 发布消息（主）
     * 
     * @param object
     * @throws RabbitMQPublishException
     */
    public void publishMessage(Serializable object) throws RabbitMQPublishException {
        if (primaryHealth) {
            try {
                primary.publish(object);

            } catch (Exception e) {
                primaryHealth = false;
                logger.error("primary publish failed:" + e.getMessage(), e);
                logger.warn("primary publish failed, try secondary.");
                publishMessageSecondary(object);
            }
        } else if (secondaryHealth) {
            publishMessageSecondary(object);
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
    public void publishMessageSecondary(Serializable object) throws RabbitMQPublishException {
        try {
            secondary.publish(object);
        } catch (Exception e) {
            logger.error("secondary publish failed:" + e.getMessage(), e);
            throw new RabbitMQPublishException(e);
        }
    }
}
