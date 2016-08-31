/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.io.File;
import java.io.Serializable;

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
public class MapDbUtil {

    public static DB openDB(String filePath, String dbName) {
        File folder = new File("filePath");
        if (folder.exists() == false) {
            folder.mkdirs();
        }

        return DBMaker.fileDB(String.format("%s/%s.db", filePath, dbName)).make();
    }

    @SuppressWarnings("unchecked")
    public static void save(DB db, String mapName, String key, Serializable value) {
        HTreeMap<String, Serializable> map =
                db.hashMap(mapName, Serializer.STRING, Serializer.JAVA).createOrOpen();

        map.put(key, value);
    }
}
