/**
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.task;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Maps;
import com.ymatou.messagebus.domain.cache.ConfigCache;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.util.CallbackSemaphoreHelper;

/**
 * 补单任务管理器
 * 
 * @author Administrator 2017年2月3日 下午5:07:11
 *
 */
public class MessageCompensateTaskManager {

    private Map<String, MessageCompensateTaskTimer> compensatTaskTimerMap = Maps.newConcurrentMap();

    private ConfigCache configCache;

    public MessageCompensateTaskManager() {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        configCache = wac.getBean(ConfigCache.class);
    }

    /**
     * 是否启动
     */
    private boolean started = false;

    /**
     * 启动所有的补单任务
     */
    public void startAll() {
        started = true;

        //初始化task
        initTasks();

    }

    public void initCompensateTask(){
        //配置更新重新init任务
        configCache.addConfigCacheListener(() -> initTasks());

        startAll();
    }

    /**
     * 初始化task，
     * 只增加task
     * 不考虑删除message code时删除任务的情况
     */
    private void initTasks() {
        if(!started){//停用 不处理
            return;
        }
        configCache.appConfigMap.values().stream().forEach(appConfig -> {
            if (!StringUtils.isEmpty(appConfig.getDispatchGroup())) {
                for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {
                    if (!Boolean.FALSE.equals(messageConfig.getEnable())) {

                        startTask(appConfig.getAppId(), messageConfig); // 启动单个补单任务
                    }
                }
            }
        });
        CallbackSemaphoreHelper.initSemaphores();
    }

    /**
     * 启动单个补单任务
     * 
     * @param appId
     * @param messageConfig
     */
    private synchronized void startTask(String appId, MessageConfig messageConfig) {
        String taskName = String.format("CompensateTask_%s_%s", appId, messageConfig.getCode());
        MessageCompensateTaskTimer compensateTaskTimer;
        if (compensatTaskTimerMap.containsKey(taskName)) {
            compensateTaskTimer = compensatTaskTimerMap.get(taskName);

            //重新设置messageconfig配置
            compensateTaskTimer.setMessageConfig(messageConfig);
        } else {
            compensateTaskTimer = new MessageCompensateTaskTimer(appId, messageConfig);
            compensateTaskTimer.start();

            compensatTaskTimerMap.put(taskName, compensateTaskTimer);
        }
    }


    /**
     * 停止所有的补单任务
     */
    public synchronized void stopAll() {
        for (Entry<String, MessageCompensateTaskTimer> taskTimer : compensatTaskTimerMap.entrySet()) {
            taskTimer.getValue().stop();
        }
        compensatTaskTimerMap.clear();
        started = false;
    }


    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
