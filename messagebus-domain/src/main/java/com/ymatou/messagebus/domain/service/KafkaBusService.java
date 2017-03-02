/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MQTypeEnum;
import com.ymatou.messagebus.infrastructure.kafka.KafkaProducerClient;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;

/**
 * Kafka消息接收发布服务
 * 
 * @author wangxudong 2016年10月11日 上午10:51:04
 *
 */
@Component
public class KafkaBusService {

    private static Logger logger = LoggerFactory.getLogger(KafkaBusService.class);

    @Resource
    private KafkaProducerClient kafkaClient;

    @Resource
    private TaskExecutor taskExecutor;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private ConfigCache configCache;

    private String appId = "mqpublish.kafka.iapi.ymatou.com";

    public void publish(Message message) {
        long startTime = System.currentTimeMillis();

        AppConfig appConfig = configCache.getAppConfig(message.getAppId());
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" +
                    message.getAppId());
        }

        if (!MQTypeEnum.Kafka.code().equals(appConfig.getMqType())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT,
                    "invalid appId:" + message.getAppId() + ", please config mqtype to kafka.");
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(message.getCode());
        if (messageConfig == null || Boolean.FALSE.equals(messageConfig.getEnable())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + message.getCode());
        }

        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.add(consumedTime, "KafkaBusService.validateMessage", appId);

        if (messageConfig.getEnableLog()) {
            logger.info("write mongo:{}", message);

            PerformanceStatisticContainer.add(() -> {
                writeMongoAsync(message, MDC.get("logPrefix"));
            }, "KafkaBusService.writeMongoAsync", appId);
        }

        PerformanceStatisticContainer.add(() -> {
            kafkaClient.sendAsync(message.getKafkaTopic(), message.getKafkaMessageKey(),
                    message.getBody());
        }, "KafkaClient.sendAsync", appId);
    }

    /**
     * 异步写消息日志
     * 
     * @param appConfig
     * @param message
     */
    private void writeMongoAsync(Message message, String requestId) {
        taskExecutor.execute(() -> {

            MDC.put("logPrefix", requestId);

            // 记录消息日志
            messageRepository.insert(message);

        });
    }
}
