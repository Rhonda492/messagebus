/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class Demo {
    public static void main(String[] args) {
        DB db = DBMaker.fileDB("message.db").make();
        System.out.println("make success!");
    }
}
