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
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.rabbitmq.ConnectionPool;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;

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
    private AppConfigRepository appConfigRepository;

    @Resource
    private TaskExecutor taskExecutor;

    @Resource
    private CallbackService callbackService;

    /**
     * 启动分发服务
     * 
     * @throws URISyntaxException
     * @throws TimeoutException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void start()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for (AppConfig appConfig : allAppConfig) {
            String dispatchGroup = appConfig.getDispatchGroup();
            if (dispatchGroup != null && dispatchGroup.contains(dispatchConfig.getGroupId())) {
                initConsumer(appConfig);
            }
        }
    }

    /**
     * 停止分发服务
     * 
     * @throws IOException
     */
    public void stop() throws IOException {
        logger.info("dispatch {} service clear consumer!", dispatchConfig.getGroupId());
        MessageConsumer.clearAll();

        logger.info("dispatch {} service clear connection pool!", dispatchConfig.getGroupId());
        ConnectionPool.clearAll();
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
    public void checkReload()
            throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException, URISyntaxException {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for (AppConfig appConfig : allAppConfig) {
            String dispatchGroup = appConfig.getDispatchGroup();
            if (dispatchGroup != null && dispatchGroup.contains(dispatchConfig.getGroupId())) {
                initConsumer(appConfig);
            } else {
                stopConsumer(appConfig);
            }
        }
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

            initSemaphore(messageConfig);

            if (!MessageConsumer.contains(appId, appCode)) {
                MessageConsumer consumer = MessageConsumer.newInstance(rabbitMQConfig, appId, appCode);
                consumer.setCallbackService(callbackService);
                consumer.run();
                logger.info("init consumer {} success.", consumer.getConsumerId());
            }
        }
    }

    /**
     * 初始化信号量
     * 
     * @param messageConfig
     */
    private void initSemaphore(MessageConfig messageConfig) {
        for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
            String consumerId = callbackConfig.getCallbackKey();
            int parallelismNum =
                    (callbackConfig.getParallelismNum() == null || callbackConfig.getParallelismNum().intValue() < 2)
                            ? 2 : callbackConfig.getParallelismNum().intValue();
            AdjustableSemaphore semaphore = SemaphorManager.get(consumerId);
            if (semaphore == null) {
                semaphore = new AdjustableSemaphore(parallelismNum);
                SemaphorManager.put(consumerId, semaphore);
            } else {
                semaphore.setMaxPermits(parallelismNum);
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
