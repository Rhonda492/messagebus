/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.CallbackInfo;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusSourceEnum;

/**
 * 补单服务
 * 
 * @author tony 2016年8月14日 下午4:08:35
 *
 */
@Component
public class CompensateService {

    private static Logger logger = LoggerFactory.getLogger(CompensateService.class);

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private CallbackServiceImpl callbackServiceImpl;


    /**
     * 检测补单合并逻辑，供调度使用
     */
    public void checkAndCompensate() {

    }

    /**
     * 检测出需要补偿的消息写入补单库
     */
    public void checkToCompensate(String appId, String code) {

    }

    /**
     * 根据Appid和Code进行补单
     */
    public void compensate(String appId, String code) {
        AppConfig appConfig = appConfigRepository.getAppConfig(appId);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + appId);
        }

        MessageConfig messageConfig = appConfig.getMessageConfig(code);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid code:" + code);
        }

        List<MessageCompensate> messageCompensatesList = messageCompensateRepository.getNeedCompensate(appId, code);
        for (MessageCompensate messageCompensate : messageCompensatesList) {
            try {
                compensateCallback(messageCompensate, messageConfig);
            } catch (Exception e) {
                logger.error(String.format("message compensate failed with appId:%s, code:%s, messageId:%s.", appId,
                        code, messageCompensate.getMessageId()), e);
            }
        }
    }

    /**
     * 补单回调
     * 
     * @param messageCompensate
     */
    private void compensateCallback(MessageCompensate messageCompensate, MessageConfig messageConfig) {
        String appId = messageCompensate.getAppId();
        String code = messageCompensate.getCode();
        String uuid = messageCompensate.getId();
        String messageId = messageCompensate.getMessageId();

        MessageStatus messageStatus =
                new MessageStatus(uuid, messageId, MessageStatusEnum.PushOk, MessageStatusSourceEnum.Compensate);

        for (CallbackInfo callbackInfo : messageCompensate.getCallbackList()) {
            CallbackConfig callbackConfig = messageConfig.getByConsumerId(callbackInfo.getCallbackKey());
            boolean result =
                    callbackServiceImpl.invokeBizSystem(messageStatus, callbackConfig, appId,
                            code, uuid, messageId, messageCompensate.getBody());
            if (result == true) {
                callbackInfo.setNewStatus(MessageCompensateStatusEnum.RetryOk.code());
            } else {
                if (messageCompensate.isRetryTimeout()) {
                    callbackInfo.setNewStatus(MessageCompensateStatusEnum.RetryFail.code());
                } else {
                    callbackInfo.setNewStatus(MessageCompensateStatusEnum.Retrying.code());
                }
            }
            callbackInfo.incRetryCount();
        }
        messageCompensate.incRetryCount();
        MessageCompensateStatusEnum compensateStatusEnum = messageCompensate.calNewStatus();
        messageCompensate.setNewStatus(compensateStatusEnum.code());
        messageCompensateRepository.update(messageCompensate);

        // 来自分发站和发布站的消息没有被分发过，所以需要记录状态
        if (messageCompensate.getSource() == MessageCompensateSourceEnum.Compensate.code()
                || messageCompensate.getSource() == MessageCompensateSourceEnum.Publish.code()) {
            messageStatusRepository.insert(messageStatus, appId);
        }

        // 刷新消息的状态
        messageRepository.updateMessageProcessStatus(appId, code, uuid,
                MessageProcessStatusEnum.from(compensateStatusEnum));
    }
}
