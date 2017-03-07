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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.util.CallbackSemaphoreHelper;
import com.ymatou.messagebus.facade.enums.MQTypeEnum;
import com.ymatou.messagebus.infrastructure.kafka.KafkaConsumerClient;
import com.ymatou.messagebus.infrastructure.thread.ScheduledExecutorHelper;

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
    private KafkaConsumerClient kafkaConsumerClient;

    @Resource
    private ConfigCache configCache;

    //是否已启动
    private static boolean started = false;



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
        started = true;

        initConsumer();
    }

    /**
     * 由于多个项目关联太紧，不能使用postconstruct 将由servlet调用
     * 初始化 kafka dispatch
     *  初始化 配置变更定时任务，消费者定时超时任务
     */
    public void initKafkaDispatch() {

        configCache.addConfigCacheListener(() -> {
            try {
                MDC.put("logPrefix", "KafkaDispatchTask|" + UUID.randomUUID().toString().replaceAll("-", ""));
                initConsumer();
                logger.info("kafka dispatch service check reload.");
            } catch (Exception e) {
                logger.error("kafka dispatch service check reload failed.", e);
            }
        });

        //启动分发服务
        start();
    }

    /**
     * 初始化消息者，重新获取配置
     */
    private void initConsumer(){

        if (!started) { // 如果停掉了，不处理
            return;
        }

        ConfigCache.appConfigMap.values().forEach(appConfig -> {
            //只处理kafka消息 并且只处理此分发组的消息
            if (MQTypeEnum.Kafka.code().equals(appConfig.getMqType()) &&
                    dispatchConfig.getGroupId().equals(appConfig.getDispatchGroup())) {
                initConsumer(appConfig);
            }
        });

        CallbackSemaphoreHelper.initSemaphores();

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

                if(StringUtils.isNotBlank(callbackConfig.getCallbackKey())){
                    kafkaConsumerClient.subscribe(topic, dispatchConfig.getGroupId(), callbackConfig.getCallbackKey(),
                            messageConfig.getPoolSize());
                }
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

        kafkaConsumerClient.unscribeAll();

        started = false;//停止
    }
}
