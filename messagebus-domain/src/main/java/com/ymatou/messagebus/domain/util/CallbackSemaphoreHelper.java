/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.util;

import org.apache.commons.lang3.StringUtils;

import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.infrastructure.thread.AdjustableSemaphore;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 回调方信号量帮助类
 * @author luoshiqian 2017/2/28 14:54
 */
public class CallbackSemaphoreHelper {

    private static Logger LOGGER = LoggerFactory.getLogger(CallbackSemaphoreHelper.class);

    /**
     * 初始化信号量
     */
    public static void initSemaphores() {
        ConfigCache.callbackConfigMap.values().stream().forEach(CallbackSemaphoreHelper::initSemaphore);

        LOGGER.info("initSemaphores success");
    }

    /**
     * 初始化信号量
     *
     * @param callbackConfig
     */
    private static void initSemaphore(CallbackConfig callbackConfig) {
        String consumerId = callbackConfig.getCallbackKey();

        // 防止sit uat数据被改 ，没有callbackkey
        if (StringUtils.isBlank(consumerId)) {
            return;
        }
        int parallelismNum =
                (callbackConfig.getParallelismNum() == null || callbackConfig.getParallelismNum() < 1)
                        ? 2 : callbackConfig.getParallelismNum();
        AdjustableSemaphore semaphore = SemaphorManager.get(consumerId);
        if (semaphore == null) {
            semaphore = new AdjustableSemaphore(parallelismNum);
            SemaphorManager.put(consumerId, semaphore);
        } else {
            semaphore.setMaxPermits(parallelismNum);
        }
    }

}
