/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.Message;
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

    private SimpleDateFormat dateFormat;

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
        String dbName = "MQ_Message_" + message.getAppId() + "_" + dateFormat.format(new Date());
        String collectionName = "Message_" + message.getAppCode();

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
    public Message getByMessageId(String appId, String code, String messageId) {
        String dbName = "MQ_Message_" + appId + "_" + dateFormat.format(new Date());
        String collectionName = "Message_" + appId + "_" + code;

        return newQuery(Message.class, dbName, collectionName).field("mid").equal(messageId).get();
    }

    /**
     * 更新消息状态
     * 
     * @param appId
     * @param code
     * @param messageId
     * @param newStatus
     */
    public void updateMessageStatus(String appId, String code, String messageId, Integer newStatus) {
        String dbName = "MQ_Message_" + appId + "_" + dateFormat.format(new Date());
        String collectionName = "Message_" + appId + "_" + code;

        Bson doc = eq("mid", messageId);
        Bson set = set("nstatus", newStatus);

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
    public void updateMessageStatusAndPublishTime(String appId, String code, String messageId, Integer newStatus) {
        String dbName = "MQ_Message_" + appId + "_" + dateFormat.format(new Date());
        String collectionName = "Message_" + appId + "_" + code;

        Bson doc = eq("mid", messageId);
        Bson set = combine(set("nstatus", newStatus), set("pushtime", new Date()));

        updateOne(dbName, collectionName, doc, set);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dateFormat = new SimpleDateFormat("yyyyMM");
    }
}
