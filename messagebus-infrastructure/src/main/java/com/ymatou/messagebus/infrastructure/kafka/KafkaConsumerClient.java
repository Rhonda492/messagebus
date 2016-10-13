/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.infrastructure.config.KafkaConsumerConfig;

public class KafkaConsumerClient {

    public static final Logger logger = LoggerFactory.getLogger(KafkaConsumerClient.class);

    private Consumer<String, String> consumer;

    @Resource
    private KafkaConsumerConfig kafkaConfig;

    @PostConstruct
    public void init() {
        consumer = new KafkaConsumer<>(kafkaConfig);
    }

    @PreDestroy
    public void destroy() {
        consumer.wakeup();
        consumer.close();
    }


}
