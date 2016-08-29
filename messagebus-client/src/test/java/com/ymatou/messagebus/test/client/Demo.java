/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import java.io.File;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class Demo {
    public static void main(String[] args) {
        File folder = new File("/usr/local/data/messagebus");
        if (folder.exists() == false) {
            folder.mkdirs();
        }


        DB db = DBMaker.fileDB("/usr/local/data/messagebus/message.db").make();
        System.out.println("make success!");

        ConcurrentMap<String, String> map = db.hashMap("message", Serializer.STRING, Serializer.STRING).createOrOpen();
        if (map.containsKey("hello")) {
            System.out.println(map.get("hello"));

        } else {
            map.put("hello", "tony");
        }

        db.close();
    }
}
