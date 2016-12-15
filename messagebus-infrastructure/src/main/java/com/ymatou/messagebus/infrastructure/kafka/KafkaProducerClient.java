/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
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

    private ExecutorService defaultProducerExecutor = new ThreadPoolExecutor(1, 2,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(10000));

    private Map<String, ExecutorService> producerExecutorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        producer = new KafkaProducer<>(kafkaConfig);
    }

    @PreDestroy
    public void destroy() {
        producer.close();
    }

    public void sendAsync(String topic, KafkaMessageKey key, String message) {
        ProducerRecord<String, String> record =
                new ProducerRecord<String, String>(topic, key.toString(), message);
        try {
            getExecutorService(topic).submit(() -> {
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error(String.format("fail to send Kafka message:%s.", record.toString()), exception);
                    }
                });
            });
        } catch (Exception e) {
            logger.error("kafka send message thread pool used up", e);
        }
    }

    /**
     * 根据topic获取到发送Kafka消息的线程池
     * 
     * @param topic
     * @return
     */
    public ExecutorService getExecutorService(String topic) {
        try {
            ExecutorService producerExecutor = producerExecutorMap.get(topic);

            if (producerExecutor == null) {
                synchronized (producerExecutorMap) {
                    if (producerExecutorMap.containsKey(topic)) {
                        producerExecutor = producerExecutorMap.get(topic);
                    } else {
                        producerExecutor = new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>(10000));
                        producerExecutorMap.put(topic, producerExecutor);
                    }
                }
            }
            return producerExecutor;
        } catch (Exception e) {
            logger.error("get kafka executor service failed with topic:" + topic, e);
            return defaultProducerExecutor;
        }
    }
}
