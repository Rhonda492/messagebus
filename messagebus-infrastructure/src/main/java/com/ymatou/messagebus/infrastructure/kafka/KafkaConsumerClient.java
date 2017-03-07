/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.ymatou.messagebus.infrastructure.config.KafkaConsumerConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangxudong 2016年10月13日 下午8:01:42
 *
 */
@Component
public class KafkaConsumerClient {

    public static final Logger logger = LoggerFactory.getLogger(KafkaConsumerClient.class);

    /**
     * 每个callbackKey 一个线程
     */
    private Map<String, KafkaConsumerThread> consumerMap = Maps.newConcurrentMap();

    @Resource
    private KafkaConsumerConfig kafkaConfig;

    @Resource
    private CallbackService callbackService;


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
     * 获取消费者线程
     * @param callbackKey
     * @return
     */
    public KafkaConsumerThread findThread(String callbackKey){
        return consumerMap.get(callbackKey);
    }

    public Map<String, KafkaConsumerThread> getKafkaConsumerThreadMap(){
        return consumerMap;
    }

    /**
     * 加synchronized 防止创建多个钱程
     * 每个callbackKey 对应一个consumer 线程
     * @param topic 主题
     * @param dispatchGroup 分发组
     * @param callbackKey kafka 订阅分组
     * @param poolSize 一次拉取大小，暂无用
     */
    public synchronized void subscribe(String topic,String dispatchGroup, String callbackKey, int poolSize) {
        if (!consumerMap.containsKey(topic)) {
            startConsumer(topic, dispatchGroup, callbackKey, poolSize);
        } else {
            KafkaConsumerThread kafkaConsumerThread = consumerMap.get(callbackKey);
            if (kafkaConsumerThread == null ||
                    kafkaConsumerThread.getState().equals(State.TERMINATED)) {
                logger.error("find consumer:{} thread terminate, start new", callbackKey);

                consumerMap.remove(callbackKey);
                startConsumer(topic, dispatchGroup, callbackKey, poolSize);
            } else {
                kafkaConsumerThread.setPoolSize(poolSize);
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

        try {
            consumerMap.clear();
        } catch (Exception e) {
            logger.error("dispatch server unscribeAll failed.", e);
        }
    }

    /**
     * 停止某个消息者线程
     * @param callbackKey
     */
    public void unscribe(String callbackKey) {
        Optional<KafkaConsumerThread> kafkaConsumerThreadOptional = Optional.fromNullable(consumerMap.get(callbackKey));
        if (kafkaConsumerThreadOptional.isPresent()) {
            kafkaConsumerThreadOptional.get().pleaseStop();
            consumerMap.remove(callbackKey);
        }
    }


    /**
     * groupId kafka 订阅分组 由 callbackKey组成
     * String.format("messagebus.dispatch.%s.%s", dispatchGroup,callbackConfig.getCallbackKey())
     * @param topic
     * @param dispatchGroup
     * @param callbackKey
     * @param poolSize
     */
    private void startConsumer(String topic,String dispatchGroup, String callbackKey, int poolSize) {

        try {
            if (!consumerMap.containsKey(callbackKey)) {
                // callbackKey 全局唯一 ,使用分发组id+callbackKey 当作groupId
                String groupId = String.format("messagebus.dispatch.%s.%s", dispatchGroup, callbackKey);
                String clientId = groupId + "." + NetUtil.getHostIp();

                kafkaConfig.put("group.id", groupId);
                kafkaConfig.put("client.id", clientId);

                KafkaConsumerThread kafkaConsumerThread =
                        new KafkaConsumerThread(topic,callbackKey, kafkaConfig, callbackService);
                kafkaConsumerThread.setName(groupId);
                kafkaConsumerThread.setPoolSize(poolSize);
                kafkaConsumerThread.start();

                consumerMap.put(callbackKey, kafkaConsumerThread);
            } else {
                //FIXME 没有意义 暂时没法重新设置拉取大小
                consumerMap.get(callbackKey).setPoolSize(poolSize);
            }
        } catch (Exception e) {
            logger.error("dispatch server subscribe failed, topic:" + topic, e);
        }
    }
}
