/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import java.util.List;

import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;

@Component
public class AppConfigRepository extends MongoRepository {

    @Resource(name = "configMongoClient")
    private MongoClient mongoClient;

    private final String dbName = "MQ_Configuration_201505";

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.mongodb.MongoRepository#getMongoClient()
     */
    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }


    /**
     * 统计出配置App的总数
     * 
     * @return
     */
    public long count() {
        Datastore datastore = getDatastore(dbName);

        Query<AppConfig> find = datastore.find(AppConfig.class);

        return find.countAll();
    }

    /**
     * 返回AppConfig
     * 
     * @return
     */
    public AppConfig getAppConfig(String appId) {
        Datastore datastore = getDatastore(dbName);

        return datastore.find(AppConfig.class).field("_id").equal(appId).get();
    }

    /**
     * @return
     */
    public List<AppConfig> getAllAppConfig() {
        Datastore datastore = getDatastore(dbName);

        return datastore.find(AppConfig.class).asList();
    }
}
