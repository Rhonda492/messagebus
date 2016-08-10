/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;

/**
 * 补偿消息仓储
 * 
 * @author wangxudong 2016年8月2日 下午6:48:54
 *
 */
@Component
public class MessageCompensateRepository extends MongoRepository {
    @Resource(name = "messageCompensateMongoClient")
    private MongoClient mongoClient;

    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    public void insert(MessageCompensate messageCompensate) {
        String dbName = "MQ_Message_Compensate";
        String collectionName = String.format("Mq_%s_%s", messageCompensate.getAppId(), messageCompensate.getCode());

        insertEntiy(dbName, collectionName, messageCompensate);
    }
}
