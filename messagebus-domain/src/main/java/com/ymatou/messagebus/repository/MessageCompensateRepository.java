/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.repository;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;
import com.ymatou.messagebus.model.MessageCompensate;

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
        String collectionName = "Mq_" + messageCompensate.getAppCode();

        insertEntiy(dbName, collectionName, messageCompensate);
    }
}
