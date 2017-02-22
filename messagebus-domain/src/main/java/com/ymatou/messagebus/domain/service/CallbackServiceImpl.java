/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.cache.AppConfigCache;
import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.model.*;
import com.ymatou.messagebus.domain.repository.AlarmRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.CompensateFacade;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.enums.*;
import com.ymatou.messagebus.facade.model.SecondCompensateReq;
import com.ymatou.messagebus.facade.model.SecondCompensateResp;
import com.ymatou.messagebus.infrastructure.logger.ErrorReportClient;
import com.ymatou.messagebus.infrastructure.mq.CallbackService;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;

/**
 * 回调服务
 * 
 * @author wangxudong 2016年8月5日 下午7:03:35
 *
 */
@Component
public class CallbackServiceImpl implements CallbackService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

    private CloseableHttpAsyncClient httpClient;

    @Resource
    private AppConfigCache appConfigCache;

    @Resource
    private AlarmRepository alarmRepository;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private ErrorReportClient errorReportClient;

    @Resource
    private TaskExecutor taskExecutor;

    @Resource(name = "compensateClient")
    private CompensateFacade compensateFacade;

    /**
     * 按照业务统计
     */
    private String monitorAppId = "mqmonitor.iapi.ymatou.com";

    /**
     * 按照机器统计性能数据
     */
    private String staticAppId = "mqstatic.iapi.ymatou.com";


    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService#invoke(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void invoke(String appId, String appCode, String messageBody, String messageId,
            String messageUuid) {
        AppConfig appConfig = appConfigCache.get(appId);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + appId);
        }

        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(appCode);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appCode:" + appCode);
        }

        List<CallbackConfig> callbackCfgList = messageConfig.getCallbackCfgList();
        if (callbackCfgList == null
                || !callbackCfgList.stream().anyMatch(x -> x.getEnable() == null || x.getEnable() == true)) {
            // throw new BizException(ErrorCode.NOT_EXIST_INVALID_CALLBACK, "appCode:" + appCode);
            return; // 没有消费者或者所有消费者都没有启用，直接放过
        }

        if (StringUtils.isEmpty(messageId)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageId can not be empty.");
        }

        if (StringUtils.isEmpty(messageUuid)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageUuid can not be empty.");
        }

        Message message = new Message();
        message.setAppId(appConfig.getAppId());
        message.setCode(messageConfig.getCode());
        message.setBody(messageBody);
        message.setMessageId(messageId);
        message.setUuid(messageUuid);

        long startTime = System.currentTimeMillis();

        invokeCore(message, messageConfig);

        // 向性能监控器汇报性能情况
        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.add(consumedTime, String.format("%s.dispatch", message.getAppCode()),
                monitorAppId);
        PerformanceStatisticContainer.add(consumedTime, "TotalDispatch", monitorAppId);
        PerformanceStatisticContainer.add(consumedTime, String.format("TotalDispatch.%s", NetUtil.getHostIp()),
                staticAppId);
    }

    @Override
    public void invokeOneCallBack(String callbackKey, String appId, String appCode, String messageBody,
            String messageId, String messageUuid, CountDownLatch countDownLatch)throws Exception {


        AppConfig appConfig = ConfigCache.appConfigMap.get(appId);
        if (appConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appId:" + appId);
        }

        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(appCode);
        if (messageConfig == null) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "invalid appCode:" + appCode);
        }

        CallbackConfig callbackConfig = ConfigCache.callbackConfigMap.get(callbackKey);

        if (callbackConfig == null
                || (callbackConfig.getEnable() != null && !callbackConfig.getEnable())) {
//             throw new BizException(ErrorCode.NOT_EXIST_INVALID_CALLBACK, "appCode:" + appCode);
            return;// 没有消费者或者所有消费者都没有启用
        }

        if (StringUtils.isEmpty(messageId)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageId can not be empty.");
        }

        if (StringUtils.isEmpty(messageUuid)) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT, "messageUuid can not be empty.");
        }

        Message message = new Message();
        message.setAppId(appConfig.getAppId());
        message.setCode(messageConfig.getCode());
        message.setBody(messageBody);
        message.setMessageId(messageId);
        message.setUuid(messageUuid);

        long startTime = System.currentTimeMillis();

        invokeOne(message, messageConfig, callbackConfig);

        // 向性能监控器汇报性能情况
        long consumedTime = System.currentTimeMillis() - startTime;

        PerformanceStatisticContainer.add(consumedTime, "SingleDispatch", monitorAppId);
        PerformanceStatisticContainer.add(consumedTime, String.format("SingleDispatch.%s", NetUtil.getHostIp()),
                staticAppId);
    }



    /**
     * 回调核心逻辑
     * 
     * @param message
     * @param messageConfig
     */
    private void invokeCore(Message message, MessageConfig messageConfig) {
        for (CallbackConfig callbackConfig : messageConfig.getCallbackCfgList()) {
            try {
                invokeOne(message, messageConfig, callbackConfig);
            } catch (Exception e) {
                logger.error(String.format("invoke biz system fail,appCode:%s, messageUuid:%s",
                        message.getAppCode(), message.getUuid()), e);
            }
        }
    }

    private void invokeOne(Message message, MessageConfig messageConfig, CallbackConfig callbackConfig) throws InterruptedException{
        if (callbackConfig.getEnable() == null || callbackConfig.getEnable()) {
            new BizSystemCallback(httpClient, message, null, callbackConfig, this)
                    .setEnableLog(messageConfig.getEnableLog()).send();
        }
    }

    /**
     * 回写成功结果
     * 
     * @param message
     * @param callbackConfig
     * @param duration
     */
    public void writeSuccessResult(CallbackModeEnum callbackMode, Message message, MessageCompensate messageCompensate,
            CallbackConfig callbackConfig, long duration, boolean enableLog) {
        String requestId = MDC.get("logPrefix");

        if (enableLog == false) {
            // logger.info("callback service write success log, uuid:{}, messageId:{}.",
            // message.getUuid(),
            // message.getMessageId());
            return;
        }

        taskExecutor.execute(() -> {
            MDC.put("logPrefix", requestId);

            try {
                MessageStatus messageStatus = MessageStatus.from(message, callbackConfig);
                messageStatus.setSource(callbackMode.toString());
                messageStatus.setStatus(MessageStatusEnum.PushOk.toString());
                messageStatus.setSuccessResult(callbackConfig.getCallbackKey(), duration, callbackConfig.getUrl());
                messageStatusRepository.insert(messageStatus, message.getAppId());

                if (CallbackModeEnum.SecondCompensate == callbackMode) {
                    MessageCompensate compensate =
                            MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Compensate);
                    compensate.incRetryCount();
                    compensate.setCompensateCount(0);
                    compensate.setRetryTime(new Date());
                    compensate.setNewStatus(MessageCompensateStatusEnum.RetryOk.code());
                    messageCompensateRepository.save(compensate);

                } else {
                    if (messageCompensate != null) {
                        messageCompensate.incRetryCount();
                        messageCompensate.incCompensateCount();
                        messageCompensate.setRetryTime(new Date());
                        messageCompensate.setNewStatus(MessageCompensateStatusEnum.RetryOk.code());
                        messageCompensateRepository.update(messageCompensate);
                    }
                }

                // TODO 处理多条结果
                messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(), message.getUuid(),
                        MessageProcessStatusEnum.Success);

            } catch (Exception e) {
                logger.error("callback writeSuccessResult fail.", e);
            }
        });
    }

    /**
     * 回写失败结果
     * 
     * @param message
     * @param callbackConfig
     * @param response
     * @param duration
     * @param throwable
     */
    public void writeFailResult(CallbackModeEnum callbackMode, Message message, MessageCompensate messageCompensate,
            CallbackConfig callbackConfig, String response, long duration, Throwable throwable) {
        String requestId = MDC.get("logPrefix");

        taskExecutor.execute(() -> {
            MDC.put("logPrefix", requestId);

            try {
                MessageStatus messageStatus = MessageStatus.from(message, callbackConfig);

                // 记录调用结果
                if (CallbackModeEnum.Dispatch == callbackMode) {
                    if (callbackConfig.getIsRetry() == null || callbackConfig.getIsRetry().intValue() > 0) {
                        // 如果需要秒级补单则调用补单站
                        if (callbackConfig.getSecondCompensateSpan() != null
                                && callbackConfig.getSecondCompensateSpan().intValue() > 0) {
                            secondCompensate(message, callbackConfig);
                        }
                    }
                }
                messageStatus.setSource(callbackMode.toString());
                messageStatus.setStatus(MessageStatusEnum.PushFail.toString());
                messageStatus.setFailResult(callbackConfig.getCallbackKey(), throwable, duration, response,
                        callbackConfig.getUrl());
                messageStatusRepository.insert(messageStatus, message.getAppId());

                // 记录补单结果
                if (CallbackModeEnum.Dispatch == callbackMode) {
                    if (callbackConfig.getIsRetry() == null || callbackConfig.getIsRetry().intValue() > 0) {
                        MessageCompensate compensate =
                                MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Dispatch);
                        messageCompensateRepository.save(compensate);
                    }
                } else if (CallbackModeEnum.SecondCompensate == callbackMode) {
                    MessageCompensate compensate =
                            MessageCompensate.from(message, callbackConfig, MessageCompensateSourceEnum.Compensate);

                    compensate.setNewStatus(MessageCompensateStatusEnum.Retrying.code());
                    compensate.incRetryCount();
                    messageCompensateRepository.save(compensate);

                } else {
                    messageCompensate.incRetryCount();
                    messageCompensate.incCompensateCount();

                    if (messageCompensate.needRetry(callbackConfig.getRetryPolicy())) {
                        messageCompensate.setRetryTime(callbackConfig.getRetryPolicy());
                        messageCompensate.setNewStatus(MessageCompensateStatusEnum.Retrying.code());
                    } else {
                        messageCompensate.setNewStatus(MessageCompensateStatusEnum.RetryFail.code());
                    }
                    messageCompensateRepository.update(messageCompensate);
                }

                // 更改消息状态
                if (CallbackModeEnum.Dispatch == callbackMode) {
                    if (callbackConfig.getIsRetry() == null || callbackConfig.getIsRetry().intValue() > 0) {
                        messageRepository.updateMessageStatusAndPublishTime(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageNewStatusEnum.DispatchToCompensate,
                                MessageProcessStatusEnum.Compensate);
                    } else {
                        messageRepository.updateMessageStatusAndPublishTime(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageNewStatusEnum.InRabbitMQ,
                                MessageProcessStatusEnum.Fail);
                    }
                } else if (CallbackModeEnum.SecondCompensate == callbackMode) {
                    messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(),
                            message.getUuid(), MessageProcessStatusEnum.Compensate);
                } else {
                    if (messageCompensate.getNewStatus() == MessageCompensateStatusEnum.RetryFail.code()) {
                        messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageProcessStatusEnum.Fail);
                    } else {
                        messageRepository.updateMessageProcessStatus(message.getAppId(), message.getCode(),
                                message.getUuid(), MessageProcessStatusEnum.Compensate);
                    }
                }
                sendErrorReport(message, callbackConfig, throwable);

            } catch (Exception e) {
                logger.error(String.format("write callback fail result fail, appcode:%s, messageid:%s",
                        message.getAppCode(), message.getUuid()), e);
            }
        });
    }



    /**
     * 发送回调错误报告
     * 
     * @param appId
     * @param code
     * @param callbackConfig
     * @param message
     * @param uuid
     * @param ex
     */
    private void sendErrorReport(Message message, CallbackConfig callbackConfig, Throwable ex) {
        String consumerId = callbackConfig.getCallbackKey();
        String callbackAppId = callbackConfig.getCallbackAppId();

        String title = String.format(
                "messagebus callback Exception, appid:%s, code:%s, consumerId:%s, url:%s, messageId:%s, uuid:%s",
                message.getAppId(), message.getCode(), consumerId, callbackConfig.getUrl(), message.getMessageId(),
                message.getUuid());
        logger.error(title, ex);

        if (!StringUtils.isEmpty(callbackAppId)) {
            logger.info("sendErrorReport subscribe appId:{}", callbackAppId);
            errorReportClient.report(title, ex, callbackAppId);
        }
    }

    /**
     * 秒级补单
     * 
     * @param message
     * @param callbackConfig
     */
    private void secondCompensate(Message message, CallbackConfig callbackConfig) {
        logger.info("seconde compensate start.");

        SecondCompensateReq req = new SecondCompensateReq();
        req.setAppId(message.getAppId());
        req.setCode(message.getCode());
        req.setMessageId(message.getMessageId());
        req.setUuid(message.getUuid());
        req.setBody(message.getBody());
        req.setConsumerId(callbackConfig.getCallbackKey());
        req.setTimeSpanSecond(callbackConfig.getSecondCompensateSpan());

        SecondCompensateResp resp = compensateFacade.secondCompensate(req);

        logger.info("seconde compensate end, resp:{}", resp);
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        httpClient = HttpAsyncClients.custom().setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(cm).build();
        httpClient.start();
    }

    /**
     * 等待所有消费者释放信号量
     * 
     * @param appId
     * @param appCode
     */
    @Override
    public void waitForSemaphore(String appId, String appCode) {
        AppConfig appConfig = appConfigCache.get(appId);
        if (appConfig == null) {
            return;
        }

        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(appCode);
        if (messageConfig == null) {
            return;
        }

        List<CallbackConfig> callbackCfgList = messageConfig.getCallbackCfgList();
        if (callbackCfgList != null && callbackCfgList.isEmpty() == false) {
            long startTime = System.currentTimeMillis();
            long waitTimeMS = 0;
            while (waitTimeMS < 1000 * 60) { // 最多等待60s
                boolean allSemaphoreRelease = true;
                for (CallbackConfig callbackConfig : callbackCfgList) {
                    AdjustableSemaphore semaphore = SemaphorManager.get(callbackConfig.getCallbackKey());
                    if (semaphore.availablePermits() < semaphore.getMaxPermits()) {
                        allSemaphoreRelease = false;
                        break;
                    }
                }

                if (allSemaphoreRelease == true) {
                    break;
                }

                waitTimeMS = System.currentTimeMillis() - startTime;
            }
        }

    }

}
