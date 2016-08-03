/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.repository;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;
import com.ymatou.messagebus.model.Message;

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


    @Override
    public void afterPropertiesSet() throws Exception {
        dateFormat = new SimpleDateFormat("yyyyMM");
    }
}
