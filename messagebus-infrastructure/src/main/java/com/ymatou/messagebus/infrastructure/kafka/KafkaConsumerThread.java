/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.*;
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

    private String callbackKey;

    private volatile long startProcessTime;

    private int sessionTimeoutMs;

    /**
     * 请求暂停
     */
    private boolean pleaseStop = false;

    public void pleaseStop() {
        this.pleaseStop = true;
    }

    public KafkaConsumerThread(String topic,String callbackKey, KafkaConsumerConfig config, CallbackService callbackService) {
        this.setTopic(topic);
        this.callbackService = callbackService;
        this.callbackKey = callbackKey;

        consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Arrays.asList(topic));

        setSessionTimeoutMs(
                Integer.valueOf(config.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")));
    }

    /**
     * 防止kafka consumer session time out rebalancing, 中断线程
     * 
     * @param currentTime
     */
    public void preventTimeout(long currentTime) {
        if (startProcessTime == 0) {
            return;
        }
        long duration = currentTime - startProcessTime;
        if (duration >= (sessionTimeoutMs - 5000)) {
            logger.info("thread will be interrupt,sessionTimout:{},duration:{},{}",sessionTimeoutMs,duration,getThreadInfo());
            startProcessTime = 0;
            this.interrupt();
        }
    }

    public String getThreadInfo() {
        return String.format("consumer thread: %s, state: %s.", toString(), getState().toString());
    }


    public void setStartProcessTime(long startProcessTime) {
        this.startProcessTime = startProcessTime;
    }


    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    @Override
    public void run() {
        logger.info("dispatch server start consume topic:{}.", this.getName());
        try {
            while (true) {
                try {
                    setStartProcessTime(0);//重新设置为0
                    ConsumerRecords<String, String> records = consumer.poll(5000);
                    String appId = null;
                    String appCode = null;

                    if(!records.isEmpty()){
                        setStartProcessTime(System.currentTimeMillis());
                        boolean isInterrupted = false;

                        for (ConsumerRecord<String, String> record : records) {
                            MDC.put("logPrefix", String.format("%s|%s", this.getName(), UUID.randomUUID().toString()));

                            logger.info("recv kafka message:{}", record);

                            KafkaMessageKey key = null;
                            try {
                                key = KafkaMessageKey.valueOf(record.key());
                                appId = key.getAppId();
                                appCode = key.getAppCode();

                                //处理一条 callback
                                callbackService.invokeOneCallBack(callbackKey,appId, appCode, record.value(), key.getMessageId(),
                                        key.getUuid(),isInterrupted);

                            }catch (InterruptedException e){
                                logger.error("thread interrupted process kafka callback timeout,recordSize:{}",
                                        records.count());
                                isInterrupted = true;
                                // 当前数据和之后数据都进入补单，使consumer继续poll,防止rebalancing
                                callbackService.invokeOneCallBack(callbackKey,appId, appCode, record.value(), key.getMessageId(),
                                        key.getUuid(),true);

                            } catch (Exception e) {
                                if (key == null) {
                                    logger.error("fail to consume kafka message, key is null", e);
                                } else {
                                    logger.error("fail to consume kafka message" + key.getUuid(), e);
                                }
                            }
                        }
                    }

                    if (this.pleaseStop) {
                        logger.info("dispatch server recv pleaseStop topic:{}, break while.", this.getName());
                        break;
                    }
                } catch (Exception e) {
                    logger.error("dispatch server exception consume topic:" + this.getName(), e);
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
    }
}
