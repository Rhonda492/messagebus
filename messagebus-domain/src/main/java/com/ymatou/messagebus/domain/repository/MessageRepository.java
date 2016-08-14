/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.util.Date;

import javax.annotation.Resource;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;

/**
 * 总线消息仓储
 * 
 * @author wangxudong 2016年8月1日 下午1:50:18
 *
 */
@Component
public class MessageRepository extends MongoRepository implements InitializingBean {
    @Resource(name = "messageMongoClient")
    private MongoClient mongoClient;


    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }


    /**
     * 插入消息
     * 
     * @param message
     */
    public void insert(Message message) {
        String dbName = "MQ_Message_" + message.getAppId() + "_" + message.getUuid().substring(0, 6);
        String collectionName = "Message_" + message.getCode();

        insertEntiy(dbName, collectionName, message);
    }


    /**
     * 获取到单条消息
     * 
     * @param appId
     * @param code
     * @param messageId
     * @return
     */
    public Message getByUuid(String appId, String code, String uuid) {
        String dbName = "MQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        return newQuery(Message.class, dbName, collectionName).field("uuid").equal(uuid).get();
    }

    /**
     * 更新消息状态
     * 
     * @param appId
     * @param code
     * @param uuid
     * @param newStatus
     */
    public void updateMessageStatus(String appId, String code, String uuid, MessageNewStatusEnum newStatus,
            MessageProcessStatusEnum processStatusEnum) {
        String dbName = "MQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        Bson doc = eq("uuid", uuid);
        Bson set = combine(set("nstatus", newStatus.code()), set("pstatus", processStatusEnum.code()));

        updateOne(dbName, collectionName, doc, set);
    }

    /**
     * 更新消息状态和发布时间
     * 
     * @param appId
     * @param code
     * @param messageId
     * @param newStatus
     */
    public void updateMessageStatusAndPublishTime(String appId, String code, String uuid,
            MessageNewStatusEnum newStatus, MessageProcessStatusEnum processStatusEnum) {
        String dbName = "MQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        Bson doc = eq("uuid", uuid);
        Bson set = combine(set("nstatus", newStatus.code()), set("pstatus", processStatusEnum.code()),
                set("pushtime", new Date()));

        updateOne(dbName, collectionName, doc, set);
    }

    /**
     * 更新消息的处理状态和发布时间
     * 
     * @param appId
     * @param code
     * @param uuid
     * @param processStatusEnum
     */
    public void updateMessageProcessStatus(String appId, String code, String uuid,
            MessageProcessStatusEnum processStatusEnum) {
        String dbName = "MQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        Bson doc = eq("uuid", uuid);
        Bson set = combine(set("pstatus", processStatusEnum.code()), set("pushtime", new Date()));

        updateOne(dbName, collectionName, doc, set);
    }

    @Override
    public void afterPropertiesSet() throws Exception {}
}
