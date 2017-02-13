/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.ymatou.messagebus.infrastructure.config.KafkaConsumerConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;

public class KafkaConsumerThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);

    private Consumer<String, String> consumer;

    private CallbackService callbackService;

    private String topic;

    private int poolSize;

    /**
     * 请求暂停
     */
    private boolean pleaseStop = false;

    public void pleaseStop() {
        this.pleaseStop = true;
    }

    public KafkaConsumerThread(String topic, KafkaConsumerConfig config, CallbackService callbackService) {
        this.setTopic(topic);
        this.callbackService = callbackService;

        consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Arrays.asList(topic));
    }

    public String getThreadInfo() {
        return String.format("consumer thread: %s, state: %s.", toString(), getState().toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        logger.info("dispatch server start consume topic:{}.", this.getName());
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(poolSize > 0 ? poolSize : 1);
                String appId = null;
                String appCode = null;
                for (ConsumerRecord<String, String> record : records) {
                    MDC.put("logPrefix", String.format("%s|%s", this.getName(), UUID.randomUUID().toString()));

                    logger.info("recv kafka message:{}", record);
                    KafkaMessageKey key = null;
                    try {
                        key = KafkaMessageKey.valueOf(record.key());
                        appId = key.getAppId();
                        appCode = key.getAppCode();
                        callbackService.invoke(appId, appCode, record.value(), key.getMessageId(),
                                key.getUuid());

                    } catch (Exception e) {
                        if (key == null) {
                            logger.error("fail to consume kafka message, key is null", e);
                        } else {
                            logger.error("fail to consume kafka message" + key.getUuid(), e);
                        }
                    }
                }

                if (!StringUtils.isEmpty(appId) && !StringUtils.isEmpty(appCode)) {
                    callbackService.waitForSemaphore(appId, appCode);
                }

                if (this.pleaseStop) {
                    logger.info("dispatch server recv pleaseStop topic:{}, break while.", this.getName());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("dispatch server exception consume topic:" + this.getName(), e);
        } finally {
            consumer.close();
        }
        logger.info("dispatch server stop consume topic:{}.", this.getName());
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;

        logger.info("KafkaConsummerThread[{}] set poolSize:{}", topic, this.poolSize);
    }
}
