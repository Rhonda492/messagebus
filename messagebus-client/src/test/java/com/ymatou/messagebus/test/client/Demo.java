/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.ymatou.messagebus.client.Message;

public class Demo {
    public static void main(String[] args) {
        File folder = new File("/usr/local/data/messagebus");
        if (folder.exists() == false) {
            folder.mkdirs();
        }


        DB db = DBMaker.fileDB("/usr/local/data/messagebus/message.db").make();
        System.out.println("make success!");

        @SuppressWarnings("unchecked")
        HTreeMap<String, Message> map =
                db.hashMap("message", Serializer.STRING, Serializer.JAVA).createOrOpen();

        map.put("hello", new Message("java", "code", "xxx", "body"));

        db.close();
        db = DBMaker.fileDB("/usr/local/data/messagebus/message.db").make();
        map = db.hashMap("message", Serializer.STRING, Serializer.JAVA).createOrOpen();

        Message message = map.get("hello");

        System.out.println(message.getBody());

        db.close();
    }
}
