/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.infrastructure.config.KafkaConsumerConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;

public class KafkaConsumerThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);

    private Consumer<KafkaMessageKey, String> consumer;

    private CallbackService callbackService;

    /**
     * 请求暂停
     */
    private boolean pleaseStop = false;

    public void pleaseStop() {
        this.pleaseStop = true;
    }

    public KafkaConsumerThread(String topic, KafkaConsumerConfig config, CallbackService callbackService) {
        this.callbackService = callbackService;

        consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Arrays.asList(topic));
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
                ConsumerRecords<KafkaMessageKey, String> records = consumer.poll(5000);

                for (ConsumerRecord<KafkaMessageKey, String> record : records) {
                    logger.info("recv kafka message:{}", record);
                    KafkaMessageKey key = record.key();
                    try {
                        callbackService.invoke(key.getAppId(), key.getAppCode(), record.value(), key.getMessageId(),
                                key.getUuid());

                    } catch (Exception e) {
                        logger.error("fail to consume kafka message" + key.getUuid(), e);
                    }
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
}
