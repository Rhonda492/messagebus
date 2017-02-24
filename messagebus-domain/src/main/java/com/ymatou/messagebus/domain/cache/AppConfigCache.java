/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;

//FIXME: AppConfig全量加载，定期刷新。Mongo配置库挂，只是不能动态更改配置。当前实现方式，Mongo配置库挂，会导致系统不可用
@Component
public class AppConfigCache implements InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(AppConfigCache.class);

    @Resource
    private AppConfigRepository appConfigRepository;

    private LoadingCache<String, Optional<AppConfig>> appConfigCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        appConfigCache = CacheBuilder.newBuilder()
                .maximumSize(1000).refreshAfterWrite(30, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Optional<AppConfig>>() {
                    @Override
                    public Optional<AppConfig> load(String key) throws Exception {
                        AppConfig appConfig = appConfigRepository.getAppConfig(key);
                        return Optional.fromNullable(appConfig);
                    }

                });
    }

    /**
     * 获取到AppConfig
     * 
     * @param appId
     * @return
     */
    public AppConfig get(String appId) {
        try {
            Optional<AppConfig> optAppConfig = appConfigCache.get(appId);
            return optAppConfig.orNull();
        } catch (Exception e) {
            logger.error("get appconfig from cache failed, appId:" + appId, e);
            return appConfigRepository.getAppConfig(appId);
        }
    }
}
