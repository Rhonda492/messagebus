/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.facade.enums.MQTypeEnum;
import com.ymatou.messagebus.infrastructure.kafka.KafkaConsumerClient;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;

/**
 * Kafka分发服务
 * 
 * @author wangxudong 2016年8月4日 下午7:10:05
 *
 */
@Component
public class KafkaDispatchService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDispatchService.class);

    @Resource
    private DispatchConfig dispatchConfig;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private CallbackService callbackService;

    @Resource
    private KafkaConsumerClient kafkaConsumerClient;

    // 定时器
    private Timer timer;



    /**
     * 启动分发服务
     * 
     * @throws URISyntaxException
     * @throws TimeoutException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void start() {
        logger.info("kafka dispatch {} service start consumer!", dispatchConfig.getGroupId());

        initConsumer();

        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkReload();
                }
            }, 60, 1000 * 60);
        }

        initKafkaConsumerTimer();

    }

    private void initKafkaConsumerTimer() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {

                long currentTime = System.currentTimeMillis();
                kafkaConsumerClient.getKafkaConsumerThreadMap().values().stream()
                        .forEach(thread -> thread.preventTimeout(currentTime));

            } catch (Exception e) {
                // 所有异常都catch到 防止异常导致定时任务停止
                logger.error("kafka consumer check timeout timer ", e);
            }
        }, 0, 1000L, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化消息者，重新获取配置
     */
    private void initConsumer(){
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        ConfigCache.resetCache(allAppConfig);

        ConfigCache.appConfigMap.values().forEach(appConfig -> {
            //只处理kafka消息 并且只处理此分发组的消息
            if (MQTypeEnum.Kafka.code().equals(appConfig.getMqType()) &&
                    dispatchConfig.getGroupId().equals(appConfig.getDispatchGroup())) {
                initConsumer(appConfig);
            }
        });

        //删除后台已删除的callbackkey 线程
        ConfigCache.needRemoveCallBackConfigList
                .forEach(callbackConfig -> kafkaConsumerClient.unscribe(callbackConfig.getCallbackKey()));
        ConfigCache.needRemoveCallBackConfigList.clear();
    }

    /**
     * 初始化消费者
     *
     * @param appConfig
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    private void initConsumer(AppConfig appConfig) {
        for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {

            String topic = appConfig.getKafkaTopic(messageConfig.getCode());
            for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
                
                kafkaConsumerClient.subscribe(topic, dispatchConfig.getGroupId(), callbackConfig.getCallbackKey(),
                        messageConfig.getPoolSize().intValue());

                // 初始化信号量
                initSemaphore(callbackConfig);
            }

        }
    }


    /**
     * 停止分发服务
     * 
     * @throws IOException
     */
    public void stop() {
        logger.info("kafka dispatch {} service stop consumer!", dispatchConfig.getGroupId());

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        kafkaConsumerClient.unscribeAll();
    }

    /**
     * 检查重启
     * 
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    public void checkReload() {
        try {
            MDC.put("logPrefix", "KafkaDispatchTask|" + UUID.randomUUID().toString().replaceAll("-", ""));
            initConsumer();
            logger.info("kafka dispatch service check reload.");
        } catch (Exception e) {
            logger.error("kafka dispatch service check reload failed.", e);
        }
    }


    /**
     * 初始化信号量
     * 
     * @param callbackConfig
     */
    private void initSemaphore(CallbackConfig callbackConfig) {

        String consumerId = callbackConfig.getCallbackKey();
        int parallelismNum =
                (callbackConfig.getParallelismNum() == null || callbackConfig.getParallelismNum().intValue() < 1)
                        ? 1 : callbackConfig.getParallelismNum().intValue();
        AdjustableSemaphore semaphore = SemaphorManager.get(consumerId);
        if (semaphore == null) {
            semaphore = new AdjustableSemaphore(parallelismNum);
            SemaphorManager.put(consumerId, semaphore);
        } else {
            semaphore.setMaxPermits(parallelismNum);
        }

    }
}
