/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageProducer;
import com.ymatou.messagebus.model.AppConfig;
import com.ymatou.messagebus.model.Message;
import com.ymatou.messagebus.model.MessageCompensate;
import com.ymatou.messagebus.model.MessageConfig;
import com.ymatou.messagebus.repository.AppConfigRepository;
import com.ymatou.messagebus.repository.MessageCompensateRepository;
import com.ymatou.messagebus.repository.MessageRepository;

/**
 * @author wangxudong 2016年8月1日 下午6:22:34
 *
 */
@Component
public class MessageBusService {

    private static Logger logger = LoggerFactory.getLogger(MessageBusService.class);

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private MessageCompensateRepository compensateRepository;

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Resource
    private TaskExecutor taskExecutor;

    /**
     * 发布消息
     * 
     * @param message
     */
    public void Publish(Message message) {
        // 消息验证
        AppConfig appConfig = appConfigRepository.getAppConfig(message.getAppId());
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + message.getAppId());
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(message.getCode());
        if (messageConfig == null || messageConfig.getEnable() == false) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + message.getCode());
        }

        // 消息日志
        messageRepository.insert(message);

        // 异步发送消息
        PublishAsync(appConfig, message);
    }

    /**
     * 异步发送消息
     * 
     * @param appConfig
     * @param message
     */
    private void PublishAsync(AppConfig appConfig, Message message) {
        taskExecutor.execute(() -> {

            logger.info(
                    "----------------------------- async publish message begin -------------------------------");

            try {
                MessageProducer producer =
                        MessageProducer.newInstance(rabbitMQConfig, message.getAppId(), message.getAppCode());
                producer.publishMessage(message.getBody());

            } catch (Exception e) {
                logger.warn("publish to rabbitmq failed with appcode:" + message.getAppCode(), e);

                try {
                    MessageCompensate messageCompensate = MessageCompensate.from(appConfig, message);
                    compensateRepository.insert(messageCompensate);
                } catch (Exception ex) {
                    logger.error("publish to mongodb failed with appcode:" + message.getAppCode(), ex);
                }
            }

            logger.info(
                    "----------------------------- async publish message end -------------------------------");
        });
    }
}
