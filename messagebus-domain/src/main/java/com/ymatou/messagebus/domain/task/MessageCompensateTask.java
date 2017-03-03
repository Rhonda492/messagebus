/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.task;

import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.DistributedLockRepository;
import com.ymatou.messagebus.domain.service.CompensateService;

/**
 * 消息补偿定时任务
 * 
 * @author wangxudong 2016年7月27日 下午4:56:26
 *
 */
public class MessageCompensateTask extends TimerTask {

    private static Logger logger = LoggerFactory.getLogger(MessageCompensateTask.class);

    private CompensateService compensateService;

    private DistributedLockRepository distributedLockRepository;

    /**
     * 分布式锁的生命周期
     */
    private final int lockLifeTimeMinute = 5;

    private String appId;

    private MessageConfig messageConfig;

    public MessageCompensateTask(String appId, MessageConfig messageConfig) {
        this.setAppId(appId);
        this.setMessageConfig(messageConfig);

        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        compensateService = (CompensateService) wac.getBean("compensateService");
        distributedLockRepository = (DistributedLockRepository) wac.getBean("distributedLockRepository");
    }

    @Override
    public void run() {
        boolean acquireLock = false;
        try {
            MDC.put("logPrefix", "MessageCompensateTask|" + UUID.randomUUID().toString().replaceAll("-", ""));

            logger.info("----------------------compensate task begin-------------------------------");
            acquireLock = distributedLockRepository.AcquireLock(getTaskId(), lockLifeTimeMinute);
            if (acquireLock) {
                logger.info("acquireLock success in task."); // 获取到锁，执行补单任务
                compensateService.checkAndCompensate(appId, messageConfig);
            } else {
                logger.info("acquireLock fail exit task.");
            }

        } catch (Exception e) {
            logger.error("compensate fail in task", e);
        } finally {
            if (acquireLock) { // 如果获取到锁，在任务执行完毕后需要释放
                distributedLockRepository.delete(getTaskId());
                logger.info("----------------------compensate task end delete lock-------------------------------");
            }
        }
    }

    public String getTaskId() {
        return String.format("CompensateTask_%s_%s", appId, messageConfig.getCode());
    }

    @Override
    public String toString() {
        return getTaskId();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public void setMessageConfig(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

}
