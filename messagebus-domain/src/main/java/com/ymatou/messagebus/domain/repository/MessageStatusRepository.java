/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoIterable;
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
    @Resource(name = "messageLogMongoClient")
    private MongoClient mongoClient;

    private static Logger logger = LoggerFactory.getLogger(MessageStatusRepository.class);

    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 重建索引
     */
    public void index() {
        MongoIterable<String> listDatabaseNames = mongoClient.listDatabaseNames();
        for (String dbName : listDatabaseNames) {
            if (dbName.startsWith("JMQ_Message_")) {
                logger.info("index db {}", dbName);
                Set<String> collectionNames = getCollectionNames(dbName);
                for (String collectionName : collectionNames) {
                    DBCollection collection = getCollection(dbName, collectionName);
                    collection.createIndex("uuid");
                    collection.createIndex("ctime");
                    if (!collectionName.startsWith("mq_subscribe")) {
                        collection.createIndex("aid");
                        collection.createIndex("code");

                        if (!dbName.equals("JMQ_Message_Compensate")) {
                            collection.createIndex("nstatus");
                            collection.createIndex("pstatus");
                        }
                    }
                }
            }
        }
    }


    /**
     * 添加消息分发记录
     * 
     * @param messageStatus
     * @param appId
     */
    public void insert(MessageStatus messageStatus, String appId) {
        String dbName = "JMQ_Message_Status_" + messageStatus.getMessageUuid().substring(0, 6);
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
        String dbName = "JMQ_Message_Status_" + uuid.substring(0, 6);
        String collectionName = "mq_subscribe_" + appId;

        return newQuery(MessageStatus.class, dbName, collectionName, ReadPreference.primaryPreferred()).field("uuid")
                .equal(uuid).field("cid").equal(consumerId).order("-ctime").get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {}

}
