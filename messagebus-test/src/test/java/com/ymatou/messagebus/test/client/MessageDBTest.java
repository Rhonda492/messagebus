/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.client;

import org.junit.Test;

import com.ymatou.messagebus.client.MessageDB;
import com.ymatou.messagebus.test.BaseTest;

public class MessageDBTest extends BaseTest {

    @Test
    public void testMessageDB() {
        MessageDB messageDB = new MessageDB("/data/messagebus/test", "testcase");

        messageDB.close();
    }
}
