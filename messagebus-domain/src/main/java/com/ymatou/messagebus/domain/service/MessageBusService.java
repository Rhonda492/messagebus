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
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.infrastructure.cluster.AutoResetHealthProxy;
import com.ymatou.messagebus.infrastructure.config.RabbitMQConfig;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageProducer;
import com.ymatou.messagebus.infrastructure.rabbitmq.RabbitMQPublishException;

/**
 * @author wangxudong 2016年8月1日 下午6:22:34
 */
@Component
public class MessageBusService implements InitializingBean {

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

    private AutoResetHealthProxy autoResetHealthProxy;

    /**
     * 发布消息
     * 
     * @param message
     */
    public void publish(Message message) {
        AppConfig appConfig = appConfigRepository.getAppConfig(message.getAppId());
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + message.getAppId());
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(message.getCode());
        if (messageConfig == null || Boolean.FALSE.equals(messageConfig.getEnable())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + message.getCode());
        }

        try {
            if (autoResetHealthProxy.isHealth()) {
                // 记录消息日志
                messageRepository.insert(message);

                // 异步发送消息
                publishToMQAsync(message, messageConfig);
            } else {
                publishToMQ(message, messageConfig, false);
            }

        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error(String.format("write message to mongo fail,appCode:%s,uuid:%s.", message.getAppCode(),
                    message.getUuid()), ex);
            autoResetHealthProxy.setBroken();
            publishToMQ(message, messageConfig, false);
        }
    }

    /**
     * 异步发送消息
     * 
     * @param appConfig
     * @param message
     */
    private void publishToMQAsync(Message message, MessageConfig messageConfig) {
        taskExecutor.execute(() -> {

            logger.info(
                    "----------------------------- async publish message begin -------------------------------");

            try {
                publishToMQ(message, messageConfig, true);

            } catch (Exception e) {
                logger.error("async publish message failed, appcode:" + message.getAppCode(), e);
            }


            logger.info(
                    "----------------------------- async publish message end -------------------------------");
        });
    }

    /**
     * 发布消息到MQ
     * 
     * @param appConfig
     * @param message
     * @throws URISyntaxException
     * @throws TimeoutException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws RabbitMQPublishException
     */
    public void publishToMQ(Message message, MessageConfig messageConfig, boolean alreadyWriteMessage) {
        try {
            MessageProducer producer =
                    MessageProducer.newInstance(rabbitMQConfig, message.getAppId(), message.getAppCode());

            if (producer.isHealth()) {
                producer.publishMessage(message.getBody(), message.getMessageId(), message.getUuid());
            } else {
                if (producer.isBroken() == false) {
                    producer.setBroken(true);
                    logger.error("rabbitmq is broken, change to mongodb, appcode:{}", message.getAppCode());
                }
                publishToCompensate(message, messageConfig, alreadyWriteMessage);
            }

        } catch (Exception e) {
            throw new BizException(ErrorCode.MESSAGE_PUBLISH_FAIL, "appcode:" + message.getMessageId(), e);
        }
    }

    /**
     * 发布消息到补偿库
     * 
     * @param appConfig
     * @param message
     */
    private void publishToCompensate(Message message, MessageConfig messageConfig, boolean alreadyWriteMessage) {
        for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
            MessageCompensate messageCompensate =
                    MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Publish);

            compensateRepository.insert(messageCompensate);
        }

        if (alreadyWriteMessage) {
            // 分发进入补单
            messageRepository.updateMessageStatus(message.getAppId(), message.getCode(), message.getUuid(),
                    MessageNewStatusEnum.PublishToCompensate, MessageProcessStatusEnum.Init);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autoResetHealthProxy = new AutoResetHealthProxy(1000 * 60);

    }
}
