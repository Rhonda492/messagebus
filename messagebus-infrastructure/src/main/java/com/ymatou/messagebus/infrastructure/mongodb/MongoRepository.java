/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.mongodb;

import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Mongo仓储基类
 * 
 * @author wangxudong 2016年8月1日 下午12:58:11
 *
 */
public abstract class MongoRepository {

    Morphia morphia = new Morphia();

    /**
     * 获取到MongoClient
     * 
     * @return
     */
    protected abstract MongoClient getMongoClient();

    /**
     * 获取到制定库名的数据源
     * 
     * @param mongoClient
     * @param dbName
     * @return
     */
    protected Datastore getDatastore(String dbName) {
        return morphia.createDatastore(getMongoClient(), dbName);
    }

    /**
     * 插入文档
     * 
     * @param dbName
     * @param collectionName
     * @param document
     */
    protected void insertEntiy(String dbName, String collectionName, Object entity) {
        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<DBObject> collection = database.getCollection(collectionName, DBObject.class);

        DBObject dbObject = morphia.toDBObject(entity);

        collection.insertOne(dbObject);
    }
}
