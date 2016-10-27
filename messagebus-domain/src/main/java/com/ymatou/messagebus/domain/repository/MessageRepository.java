/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.bson.conversions.Bson;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoIterable;
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

    private SimpleDateFormat simpleDateFormat;

    private static Logger logger = LoggerFactory.getLogger(MessageRepository.class);


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
     * 插入消息
     * 
     * @param message
     */
    public void insert(Message message) {
        String dbName = "JMQ_Message_" + message.getAppId() + "_" + message.getUuid().substring(0, 6);
        String collectionName = "Message_" + message.getCode();

        insertEntiy(dbName, collectionName, message);
    }

    /**
     * 删除消息
     * 
     * @param message
     */
    public void delete(Message message) {
        String dbName = "JMQ_Message_" + message.getAppId() + "_" + message.getUuid().substring(0, 6);
        String collectionName = "Message_" + message.getCode();

        Bson filter = eq("uuid", message.getUuid());
        deleteOne(dbName, collectionName, filter);
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
        String dbName = "JMQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        return newQuery(Message.class, dbName, collectionName, ReadPreference.primaryPreferred()).field("uuid")
                .equal(uuid).get();
    }

    /**
     * 获取到单条消息
     * 
     * @param appId
     * @param code
     * @param messageId
     * @return
     */
    public Message getByMessageId(String appId, String code, String colSuffix, String messageId) {
        String dbName = "JMQ_Message_" + appId + "_" + colSuffix;
        String collectionName = "Message_" + code;

        return newQuery(Message.class, dbName, collectionName, ReadPreference.primaryPreferred()).field("mid")
                .equal(messageId).get();
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
        String dbName = "JMQ_Message_" + appId + "_" + uuid.substring(0, 6);
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
        String dbName = "JMQ_Message_" + appId + "_" + uuid.substring(0, 6);
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
        String dbName = "JMQ_Message_" + appId + "_" + uuid.substring(0, 6);
        String collectionName = "Message_" + code;

        Bson doc = eq("uuid", uuid);
        Bson set = combine(set("pstatus", processStatusEnum.code()), set("pushtime", new Date()));

        updateOne(dbName, collectionName, doc, set);
    }

    /**
     * 检测出需要补单的消息
     * 
     * @param appId
     * @param code
     * @return
     */
    public List<Message> getNeedToCompensate(String appId, String code) {
        String dbName = "JMQ_Message_" + appId + "_";
        String collectionName = "Message_" + code;

        Calendar calendarBegin = Calendar.getInstance();
        calendarBegin.add(Calendar.HOUR, -48);

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.MINUTE, -5);



        Query<Message> query =
                newQuery(Message.class, dbName + getTableSuffix(), collectionName, ReadPreference.primaryPreferred());
        query.and(
                query.criteria("nstatus").equal(MessageNewStatusEnum.InRabbitMQ.code()),
                query.criteria("pstatus").equal(MessageProcessStatusEnum.Init.code()),
                query.criteria("ctime").greaterThan(calendarBegin.getTime()),
                query.criteria("ctime").lessThan(calendarEnd.getTime()));
        List<Message> curMonthList = query.asList();

        // 如果是跨月第一天，则补单需要查询上月的数据
        if (calendarEnd.get(Calendar.DATE) == 1) {
            Query<Message> queryLast =
                    newQuery(Message.class, dbName + getTableSuffixLastMonth(), collectionName,
                            ReadPreference.primaryPreferred());
            queryLast.and(
                    queryLast.criteria("nstatus").equal(MessageNewStatusEnum.InRabbitMQ.code()),
                    queryLast.criteria("pstatus").equal(MessageProcessStatusEnum.Init.code()),
                    queryLast.criteria("ctime").greaterThan(calendarBegin.getTime()),
                    queryLast.criteria("ctime").lessThan(calendarEnd.getTime()));
            List<Message> lastMonthList = queryLast.asList();

            curMonthList.addAll(lastMonthList);
        }

        return curMonthList;
    }

    /**
     * 获得当月表后缀
     * 
     * @return
     */
    public String getTableSuffix() {
        Calendar calendarNow = Calendar.getInstance();
        return simpleDateFormat.format(calendarNow.getTime());
    }

    /**
     * 获取到上月表后缀
     * 
     * @return
     */
    public String getTableSuffixLastMonth() {
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MONTH, -1);

        return simpleDateFormat.format(calendarNow.getTime());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        simpleDateFormat = new SimpleDateFormat("yyyyMM");
    }
}
