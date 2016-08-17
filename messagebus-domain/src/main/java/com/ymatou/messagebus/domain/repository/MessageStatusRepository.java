/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;

/**
 * 消息分发记录表
 * 
 * @author wangxudong 2016年8月11日 下午2:01:01
 *
 */
@Component
public class MessageStatusRepository extends MongoRepository implements InitializingBean {
    @Resource(name = "messageMongoClient")
    private MongoClient mongoClient;


    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 添加消息分发记录
     * 
     * @param messageStatus
     * @param appId
     */
    public void insert(MessageStatus messageStatus, String appId) {
        String dbName = "MQ_Message_Status_" + messageStatus.getMessageUuid().substring(0, 6);
        String collectionName = "mq_subscribe_" + appId;

        insertEntiy(dbName, collectionName, messageStatus);
    }

    /**
     * 获取到单条消息结果
     * 
     * @param appId
     * @param code
     * @param messageId
     * @return
     */
    public MessageStatus getByUuid(String appId, String uuid, String consumerId) {
        String dbName = "MQ_Message_Status_" + uuid.substring(0, 6);
        String collectionName = "mq_subscribe_" + appId;

        return newQuery(MessageStatus.class, dbName, collectionName, ReadPreference.primaryPreferred()).field("uuid")
                .equal(uuid).field("cid").equal(consumerId).get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {}

}
