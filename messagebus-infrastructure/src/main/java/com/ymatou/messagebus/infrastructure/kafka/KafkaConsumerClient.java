/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.infrastructure.config.KafkaConsumerConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.net.NetUtil;

/**
 * @author wangxudong 2016年10月13日 下午8:01:42
 *
 */
@Component
public class KafkaConsumerClient {

    public static final Logger logger = LoggerFactory.getLogger(KafkaConsumerClient.class);

    private Map<String, KafkaConsumerThread> consumerMap = new HashMap<>();

    @Resource
    private KafkaConsumerConfig kafkaConfig;

    @Resource
    private CallbackService callbackService;

    /**
     * 读写锁
     */
    private ReadWriteLock myLock = new ReentrantReadWriteLock();

    /**
     * 获取到消费者信息
     * 
     * @return
     */
    public Map<String, String> getConsumerInfo() {
        Map<String, String> mapInfo = new HashMap<>();
        for (Map.Entry<String, KafkaConsumerThread> thread : consumerMap.entrySet()) {
            mapInfo.put(thread.getKey(), thread.getValue().getThreadInfo());
        }

        return mapInfo;
    }

    /**
     * 每个主题由一个消费者线程订阅
     * 
     * @param topic
     */
    public void subscribe(String topic, String groupId) {
        if (!consumerMap.containsKey(topic)) {
            startConsumer(topic, groupId);
        } else {
            KafkaConsumerThread kafkaConsumerThread = consumerMap.get(topic);
            if (kafkaConsumerThread == null ||
                    kafkaConsumerThread.getState().equals(State.TERMINATED)) {
                logger.error("find consumer:{} thread terminate, start new", topic);

                consumerMap.remove(topic);
                startConsumer(topic, groupId);
            }
        }
    }

    /**
     * 停止所有的消费者，停止消费线程
     */
    public void unscribeAll() {
        logger.info("kafka dispatch service unscribe all consumer.");

        for (Map.Entry<String, KafkaConsumerThread> consumer : consumerMap.entrySet()) {
            consumer.getValue().pleaseStop();
        }

        myLock.writeLock().lock();
        try {
            consumerMap.clear();
        } catch (Exception e) {
            logger.error("dispatch server unscribeAll failed.", e);
        } finally {
            myLock.writeLock().unlock();
        }
    }

    /**
     * 启动消费者线程
     * 
     * @param topic
     */
    private void startConsumer(String topic, String groupId) {
        myLock.writeLock().lock();
        try {
            if (!consumerMap.containsKey(topic)) {
                kafkaConfig.put("group.id", String.format("messagebus.dispatch.%s", groupId));
                kafkaConfig.put("client.id",
                        String.format("messagebus.dispatch.%s.%s.%s", topic, groupId, NetUtil.getHostIp()));

                KafkaConsumerThread kafkaConsumerThread =
                        new KafkaConsumerThread(topic, kafkaConfig, callbackService);
                kafkaConsumerThread.setName(topic);
                kafkaConsumerThread.start();

                consumerMap.put(topic, kafkaConsumerThread);
            }
        } catch (Exception e) {
            logger.error("dispatch server subscribe failed, topic:" + topic, e);
        } finally {
            myLock.writeLock().unlock();
        }
    }
}
