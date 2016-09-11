/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.HTreeMap.KeySet;
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
     * 数据库
     */
    private DB db;

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

        File folder = new File(this.filePath);
        if (folder.exists() == false) {
            folder.mkdirs();
        }
        db = DBMaker.fileDB(String.format("%s/%s.db", this.filePath, dbName)).closeOnJvmShutdown()
                .checksumHeaderBypass()
                .make();
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
        HTreeMap<String, T> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        map.put(key, value);
        db.commit();
    }

    /**
     * 获取到数据总数
     * 
     * @param mapName
     * @return
     */
    @SuppressWarnings("unchecked")
    public int count(String mapName) {
        HTreeMap<String, Serializable> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        int cnt = map.size();

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
        HTreeMap<String, T> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        T entity = map.get(key);

        return entity;
    }

    /**
     * 删除数据库记录
     * 
     * @param mapName
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T delete(String mapName, String key) {
        HTreeMap<String, T> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        T entity = map.remove(key);
        db.commit();

        return entity;
    }

    /**
     * 获取到消息键迭代器
     * 
     * @param mapName
     * @return
     */
    @SuppressWarnings("unchecked")
    public Iterator<String> getKeysIterator(String mapName) {
        HTreeMap<String, Serializable> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        KeySet<String> keys = map.getKeys();
        return keys.iterator();
    }

    /**
     * 清空消息
     * 
     * @param mapName
     */
    @SuppressWarnings("unchecked")
    public void clear(String mapName) {
        HTreeMap<String, Serializable> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        map.clear();
        db.commit();
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (db != null && db.isClosed() == false) {
            db.close();
        }
    }
}
