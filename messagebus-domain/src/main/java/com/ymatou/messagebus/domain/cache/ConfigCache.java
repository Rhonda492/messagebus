/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.infrastructure.thread.ScheduledExecutorHelper;

/**
 * 配置缓存 重新设置缓存，找出需要删除的等
 * 
 * @author luoshiqian 2017/2/21 14:05
 */
@Component
public class ConfigCache{

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCache.class);

    @Autowired
    private AppConfigRepository appConfigRepository;

    // app配置map缓存 key为 appId
    public static Map<String, AppConfig> appConfigMap = Maps.newConcurrentMap();

    // 回调配置map缓存 key为 callbackKey
    public static Map<String, CallbackConfig> callbackConfigMap = Maps.newConcurrentMap();

    // 配置删除的回调配置
    public static List<CallbackConfig> needRemoveCallBackConfigList = Lists.newArrayList();

    private List<ConfigReloadListener> configReloadListeners = Lists.newArrayList();

    private void resetCache(List<AppConfig> appConfigs) {

        if (appConfigMap.isEmpty()) {

            appConfigMap = listToMap(appConfigs, AppConfig::getAppId);

            callbackConfigMap = listToMap(getCallbackConfigsFromApps(appConfigs), CallbackConfig::getCallbackKey);
        } else {
            // 重新设置
            List<CallbackConfig> callbackConfigNewList = getCallbackConfigsFromApps(appConfigs);

            needRemoveCallBackConfigList = callbackConfigMap.values().stream()
                    .filter(callbackConfig -> !callbackConfigNewList.contains(callbackConfig))
                    .collect(Collectors.toList());

            appConfigMap = listToMap(appConfigs, AppConfig::getAppId);


            callbackConfigMap = listToMap(callbackConfigNewList, CallbackConfig::getCallbackKey);

        }

    }


    public static <T> void forEachList(List<T> list, Consumer<T> consumer) {
        if (!list.isEmpty()) {
            list.forEach(t -> consumer.accept(t));
        }
    }

    // 将list转为map工具
    public static <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> function) {
        Map<K, V> map = Maps.newConcurrentMap();
        forEachList(list, v -> {
            K k = function.apply(v);
            if (k != null) {
                map.put(k, v);
            }
        });

        return map;
    }


    /**
     * 从appconfigs中获取 callbackConfig list
     * 
     * @param appConfigs
     * @return
     */
    public static List<CallbackConfig> getCallbackConfigsFromApps(List<AppConfig> appConfigs) {

        return appConfigs.stream().flatMap(appConfig -> appConfig.getMessageCfgList().stream()
                .flatMap(messageConfig -> messageConfig.getCallbackCfgList().stream())).collect(Collectors.toList());
    }

    @Order(1)
    @PostConstruct
    public void initConfigCache() throws Exception {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        if(allAppConfig != null && !allAppConfig.isEmpty()){
            resetCache(allAppConfig);
        }

        ScheduledExecutorHelper.newSingleThreadScheduledExecutor("reload-config-cache").scheduleAtFixedRate(
                () -> {
                    try {
                        List<AppConfig> appConfigList = appConfigRepository.getAllAppConfig();
                        resetCache(appConfigList);
                        //回调通知
                        configReloadListeners.stream().forEach(ConfigReloadListener::callback);

                        LOGGER.info("reload-config-cache and callback success!");
                    } catch (Exception e) {
                        // 所有异常都catch到 防止异常导致定时任务停止
                        LOGGER.error("reload-config-cache and callback error ", e);
                    }
                }, 60, 60L * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * appid获取appconfig
     * @param appId
     * @return
     */
    public AppConfig getAppConfig(String appId) {
        return appConfigMap.get(appId);
    }

    /**
     * 增加配置监听器，配置刷新 就通知
     * @param listener
     */
    public void addConfigCacheListener(ConfigReloadListener listener){
        configReloadListeners.add(listener);
    }
}
