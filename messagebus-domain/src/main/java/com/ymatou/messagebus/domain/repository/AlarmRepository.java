/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.Alarm;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;

@Component
public class AlarmRepository extends MongoRepository {

    @Resource(name = "configMongoClient")
    private MongoClient mongoClient;

    private final String dbName = "MQ_Alarm";

    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 根据ConsumerId获取到报警配置
     * 
     * @param consumerId
     * @return
     */
    public Alarm getByConsumerId(String consumerId) {
        Datastore datastore = getDatastore(dbName);

        return datastore.find(Alarm.class).field("_id").equal(consumerId).get();
    }
}
