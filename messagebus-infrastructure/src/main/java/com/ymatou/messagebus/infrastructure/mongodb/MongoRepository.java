/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.mongodb;


import org.bson.conversions.Bson;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

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


    /**
     * 更新文档
     * 
     * @param dbName
     * @param collectionName
     * @param document
     */
    protected UpdateResult updateOne(String dbName, String collectionName, Bson doc, Bson res) {
        MongoClient mongoClient = getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<DBObject> collection = database.getCollection(collectionName, DBObject.class);


        return collection.updateOne(doc, res);
    }

    /**
     * 创建查询
     * 
     * @param type
     * @param dbName
     * @param collectionName
     * @return
     */
    protected <T> Query<T> newQuery(final Class<T> type, String dbName, String collectionName) {
        Datastore datastore = getDatastore(dbName);
        DBCollection collection = datastore.getDB().getCollection(collectionName);
        QueryFactory queryFactory = datastore.getQueryFactory();

        return queryFactory.createQuery(datastore, collection, type);
    }
}
