/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;


import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;
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

    private String dbName = "JMQ_Message_Compensate";

    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 重建索引
     */
    public void index() {
        Set<String> collectionNames = getCollectionNames(dbName);
        for (String collectionName : collectionNames) {
            DBCollection collection = getCollection(dbName, collectionName);
            collection.createIndex("uuid");
            collection.createIndex("ctime");
        }
    }

    /**
     * 插入补单库
     * 
     * @param messageCompensate
     */
    public void insert(MessageCompensate messageCompensate) {
        String collectionName = String.format("Mq_%s_%s", messageCompensate.getAppId(), messageCompensate.getCode());

        insertEntiy(dbName, collectionName, messageCompensate);
    }

    /**
     * 更新补单信息
     * 
     * @param messageCompensate
     */
    public void update(MessageCompensate messageCompensate) {
        String collectionName = String.format("Mq_%s_%s", messageCompensate.getAppId(), messageCompensate.getCode());
        DatastoreImpl datastore = (DatastoreImpl) getDatastore(dbName);

        datastore.save(collectionName, messageCompensate);
    }

    /**
     * 查找消息补偿信息
     * 
     * @param appId
     * @param code
     * @param messageUuid
     * @return
     */
    public MessageCompensate getByUuid(String appId, String code, String messageUuid) {
        String collectionName = String.format("Mq_%s_%s", appId, code);

        return newQuery(MessageCompensate.class, dbName, collectionName, ReadPreference.primary())
                .field("uuid").equal(messageUuid).get();
    }

    /**
     * 获取到需要补单的消息
     * 
     * @param appId
     * @param code
     * @return
     */
    public List<MessageCompensate> getNeedCompensate(String appId, String code) {
        String collectionName = String.format("Mq_%s_%s", appId, code);

        Query<MessageCompensate> query =
                newQuery(MessageCompensate.class, dbName, collectionName, ReadPreference.primaryPreferred());
        query.or(
                query.criteria("nstatus").equal(MessageCompensateStatusEnum.NotRetry.code()),
                query.criteria("nstatus").equal(MessageCompensateStatusEnum.Retrying.code()));

        return query.asList();
    }
}
