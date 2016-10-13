/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.infrastructure.config.KafkaProducerConfig;


@Component
public class KafkaProducerClient {

    public static final Logger logger = LoggerFactory.getLogger(KafkaProducerClient.class);

    private Producer<String, String> producer;

    @Resource
    private KafkaProducerConfig kafkaConfig;

    private ExecutorService producerExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(10000));

    @PostConstruct
    public void init() {
        producer = new KafkaProducer<>(kafkaConfig);
    }

    @PreDestroy
    public void destroy() {
        producer.close();
    }

    public void sendAsync(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, message);
        try {
            producerExecutor.submit(() -> {
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Failed to send Kafka message:{}", record);
                    }
                });
            });
        } catch (Exception e) {
            logger.error("Kafka send message thread pool used up", e);
        }
    }
}
