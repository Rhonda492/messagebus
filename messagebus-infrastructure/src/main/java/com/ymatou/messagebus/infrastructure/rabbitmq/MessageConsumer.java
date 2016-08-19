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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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

    private String exchange;
    private String queue;

    private CallbackService callbackService;


    /**
     * 消费者Map key={exchange}_{queue}
     * 
     * @see com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer#getConsumerId
     */
    private static Map<String, MessageConsumer> messageConsumerMap = new HashMap<String, MessageConsumer>();

    /**
     * 获取到消费者列表
     * 
     * @return
     */
    public static Map<String, MessageConsumer> getConsumerMap() {
        return messageConsumerMap;
    }

    /**
     * 判断是否包含指定消费者
     * 
     * @param exchange
     * @param queue
     * @return
     */
    public static boolean contains(String exchange, String queue) {
        String key = getConsumerId(exchange, queue);
        return messageConsumerMap.containsKey(key);
    }

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
    private MessageConsumer(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
        this.exchange = exchange;
        this.queue = queue;
        this.primary = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getPrimaryUri(), exchange, queue);
        this.secondary = EndPoint.newInstance(EndPointEnum.CONSUMER, rabbitMQConfig.getSecondaryUri(), exchange, queue);
    }

    /**
     * 获取到消费者Id
     * 
     * @return
     */
    public String getConsumerId() {
        return getConsumerId(exchange, queue);
    }

    /**
     * 拼装ConsumerId
     * 
     * @param exchange
     * @param queue
     * @return
     */
    private static String getConsumerId(String exchange, String queue) {
        return String.format("%s_%s", exchange, queue);
    }

    /**
     * 获取Consumer
     * 
     * @param exchange
     * @param queue
     * @return
     */
    public static MessageConsumer getConsumer(String exchange, String queue) {
        String key = getConsumerId(exchange, queue);

        return messageConsumerMap.get(key);
    }

    /**
     * 清空消费者列表
     */
    public static void clear() {
        messageConsumerMap.clear();
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
    public static MessageConsumer newInstance(RabbitMQConfig rabbitMQConfig, String exchange, String queue)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        String key = getConsumerId(exchange, queue);
        MessageConsumer consumer = messageConsumerMap.get(key);
        if (consumer == null) {
            synchronized (messageConsumerMap) {
                if (messageConsumerMap.containsKey(key)) {
                    return messageConsumerMap.get(key);
                } else {
                    consumer = new MessageConsumer(rabbitMQConfig, exchange, queue);
                    messageConsumerMap.put(key, consumer);
                    return consumer;
                }
            }
        } else {
            return consumer;
        }
    }


    /*
     * 启动消费者
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            primary.consume(this);
            secondary.consume(this);
        } catch (IOException e) {
            logger.error("consumer run failed.", e);
        }
    }

    /**
     * 停止消费
     */
    public void stop() {
        try {
            primary.close();
            secondary.close();
        } catch (Exception e) {
            logger.error(String.format("message consumer: %s stop", getConsumerId()), e);
        } finally {
            String key = getConsumerId(exchange, queue);
            synchronized (messageConsumerMap) {
                messageConsumerMap.remove(key);
            }
        }
    }

    /**
     * 设置回调服务
     * 
     * @param callbackService
     */
    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope env, BasicProperties props, byte[] body)
            throws IOException {
        String message = (String) SerializationUtils.deserialize(body);
        String messageId = props.getMessageId();
        String correlationId = props.getCorrelationId();
        String type = props.getType();

        MDC.put("logPrefix", "MessageConsumer|" + correlationId);

        logger.info("consumer [{}] receive rabbitmq[{}] messageId:{}, correlationId:{}, message:{}",
                getConsumerId(), type, messageId, correlationId, message);

        try {
            callbackService.invoke(exchange, queue, message, messageId, correlationId);
        } catch (Exception e) {
            logger.error(String.format("consumer %s callback failed", getConsumerId()), e);
        } finally {
            if (type.equals("primary")) {
                primary.getChannel().basicAck(env.getDeliveryTag(), true);
            } else {
                secondary.getChannel().basicAck(env.getDeliveryTag(), true);
            }
        }
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        logger.info("Consumer " + consumerTag + " registered.");

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
        logger.info("Consumer " + consumerTag + " shutdown.");
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        logger.info("Consumer " + consumerTag + " recover ok.");

    }
}
