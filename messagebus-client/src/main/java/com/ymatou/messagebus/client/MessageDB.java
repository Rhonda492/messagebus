/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/**
 * MapDB工具类
 * 
 * @author wangxudong 2016年8月31日 下午3:58:39
 *
 */
public class MessageDB {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 数据库名称
     */
    private String dbName;


    /**
     * 默认的本地消息存储路径
     */
    private final static String DEFUALT_MESSAGE_DB_PATH = "/data/messagebus";

    /**
     * 构造数据库
     * 
     * @param filePath
     * @param dbName
     */
    public MessageDB(String filePath, String dbName) {
        this.filePath = StringUtils.isEmpty(filePath) ? DEFUALT_MESSAGE_DB_PATH : trimSuffix(filePath);
        this.dbName = dbName;

        File folder = new File(this.filePath);
        if (folder.exists() == false) {
            folder.mkdirs();
        }
        DBMaker.fileDB(String.format("%s/%s.db", this.filePath, dbName)).make().close();;
    }

    /**
     * 
     * @param filePath
     * @return
     */
    private String trimSuffix(String filePath) {
        if (filePath.endsWith("/")) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /**
     * 保存数据
     * 
     * @param mapName
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    public <T> void save(String mapName, String key, T value) {
        DB db = DBMaker.fileDB(String.format("%s/%s.db", filePath, dbName)).make();
        HTreeMap<String, T> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        map.put(key, value);
        db.close();
    }

    /**
     * 获取到数据总数
     * 
     * @param mapName
     * @return
     */
    @SuppressWarnings("unchecked")
    public int count(String mapName) {
        DB db = DBMaker.fileDB(String.format("%s/%s.db", filePath, dbName)).make();

        HTreeMap<String, Serializable> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        int cnt = map.size();
        db.close();

        return cnt;
    }

    /**
     * 获取数据库记录
     * 
     * @param mapName
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String mapName, String key) {
        DB db = DBMaker.fileDB(String.format("%s/%s.db", filePath, dbName)).make();

        HTreeMap<String, T> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        T entity = map.get(key);
        db.close();

        return entity;
    }
}
