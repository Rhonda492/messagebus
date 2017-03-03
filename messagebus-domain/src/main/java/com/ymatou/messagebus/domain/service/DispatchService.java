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
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.util.CallbackSemaphoreHelper;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.rabbitmq.ConnectionPool;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;

/**
 * 分发服务
 * 
 * @author wangxudong 2016年8月4日 下午7:10:05
 *
 */
@Component
public class DispatchService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchService.class);

    @Resource
    private DispatchConfig dispatchConfig;

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Resource
    private CallbackService callbackService;

    @Resource
    private ConfigCache configCache;

    //是否已启动
    private static boolean started = false;

    /**
     * 启动分发服务
     *
     */
    public void start() throws Exception {

        started = true;

        initRabbitMqConsumer();

    }

    /**
     * 由于多个项目关联太紧，不能使用postconstruct 将由servlet调用 初始化 rabbit dispatch 初始化 配置变更定时任务
     */
    public void initRabbitMqDispatch() throws Exception {
        configCache.addConfigCacheListener(() -> {
            try {
                MDC.put("logPrefix", "MessageDispatchTask|" + UUID.randomUUID().toString().replaceAll("-", ""));

                initRabbitMqConsumer();
                logger.info("rabbitmq dispatch service check reload.");
            } catch (Exception e) {
                logger.error("rabbitmq dispatch service check reload failed.", e);
            }
        });

        // 启动分发服务
        start();
    }

    private void initRabbitMqConsumer() throws Exception{

        if(!started){//停用不处理
            return;
        }

        for (AppConfig appConfig : ConfigCache.appConfigMap.values()) {
            String dispatchGroup = appConfig.getDispatchGroup();
            if (dispatchGroup != null && dispatchGroup.contains(dispatchConfig.getGroupId())) {
                initConsumer(appConfig);
            }else {
                //修改app分组后 删除消费者  不大可能发生 目前不允许修改分组
                stopConsumer(appConfig);
            }
        }
        CallbackSemaphoreHelper.initSemaphores();
    }

    /**
     * 停止分发服务
     * 
     * @throws IOException
     */
    public void stop() throws IOException {

        started = false;
        logger.info("dispatch {} service clear consumer!", dispatchConfig.getGroupId());
        MessageConsumer.clearAll();

        logger.info("dispatch {} service clear connection pool!", dispatchConfig.getGroupId());
        ConnectionPool.clearAll();
    }

    @PreDestroy
    public void destory() throws Exception{
        stop();
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
    private void initConsumer(AppConfig appConfig)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {
            String appCode = appConfig.getAppCode(messageConfig.getCode());
            String appId = appConfig.getAppId();

            if (!MessageConsumer.contains(appId, appCode)) {
                MessageConsumer consumer = MessageConsumer.newInstance(rabbitMQConfig, appId, appCode);
                consumer.setCallbackService(callbackService);
                consumer.run();
                logger.info("init consumer {} success.", consumer.getConsumerId());
            }
        }
    }

    /**
     * 停止消费者
     * 
     * @param appConfig
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    private void stopConsumer(AppConfig appConfig)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        for (MessageConfig callbackConfig : appConfig.getMessageCfgList()) {
            String appId = appConfig.getAppId();
            String appCode = appConfig.getAppCode(callbackConfig.getCode());

            if (MessageConsumer.contains(appId, appCode)) {
                MessageConsumer consumer = MessageConsumer.getConsumer(appId, appCode);
                consumer.stop();
                logger.info("stop consumer {} success.", consumer.getConsumerId());
            }
        }
    }
}
