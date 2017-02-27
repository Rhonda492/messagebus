/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.cache;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;

/**
 * 配置缓存 重新设置缓存，找出需要删除的等
 * 
 * @author luoshiqian 2017/2/21 14:05
 */
public class ConfigCache {

    // app配置map缓存 key为 appId
    public static Map<String, AppConfig> appConfigMap = Maps.newConcurrentMap();

    // 回调配置map缓存 key为 callbackKey
    public static Map<String, CallbackConfig> callbackConfigMap = Maps.newConcurrentMap();

    // 配置删除的回调配置
    public static List<CallbackConfig> needRemoveCallBackConfigList = Lists.newArrayList();

    public static void resetCache(List<AppConfig> appConfigs) {

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

}
