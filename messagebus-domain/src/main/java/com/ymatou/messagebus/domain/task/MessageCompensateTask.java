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
     * 分布式锁的类型
     */
    private final String lockType = "Compensate";

    /**
     * 分布式锁的生命周期
     */
    private final int lockLifeTimeMinute = 5;

    public MessageCompensateTask() {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        compensateService = (CompensateService) wac.getBean("compensateService");
        distributedLockRepository = (DistributedLockRepository) wac.getBean("distributedLockRepository");
    }

    @Override
    public void run() {
        try {
            MDC.put("logPrefix", "MessageCompensateTask|" + UUID.randomUUID().toString().replaceAll("-", ""));

            logger.info("----------------------compensate task begin-------------------------------");
            boolean acquireLock = distributedLockRepository.AcquireLock(lockType, lockLifeTimeMinute);
            if (acquireLock == false) {
                logger.info("acquireLock fail exit task.");
            } else {
                logger.info("acquireLock success in task.");

                compensateService.initSemaphore();

                compensateService.checkAndCompensate();
            }

        } catch (Exception e) {
            logger.error("compensate fail in task", e);
        } finally {
            distributedLockRepository.delete(lockType);
            logger.info("----------------------compensate task end delete lock-------------------------------");
        }
    }

}
