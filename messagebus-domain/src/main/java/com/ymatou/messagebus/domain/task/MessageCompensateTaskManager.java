/**
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;

/**
 * 补单任务管理器
 * 
 * @author Administrator 2017年2月3日 下午5:07:11
 *
 */
public class MessageCompensateTaskManager {

    private Map<String, MessageCompensateTaskTimer> compensatTaskTimerMap = new HashMap<>();

    private AppConfigRepository appConfigRepository;

    public MessageCompensateTaskManager() {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        setAppConfigRepository((AppConfigRepository) wac.getBean("appConfigRepository"));
    }

    /**
     * 是否启动
     */
    private boolean started = false;

    /**
     * 启动所有的补单任务
     */
    public void startAll() {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for (AppConfig appConfig : allAppConfig) {
            if (!StringUtils.isEmpty(appConfig.getDispatchGroup())) {
                for (MessageConfig messageConfig : appConfig.getMessageCfgList()) {
                    if (!Boolean.FALSE.equals(messageConfig.getEnable())) {

                        startTask(appConfig.getAppId(), messageConfig); // 启动单个补单任务
                    }
                }

            }
        }
        started = true;
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

    public AppConfigRepository getAppConfigRepository() {
        return appConfigRepository;
    }

    public void setAppConfigRepository(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
