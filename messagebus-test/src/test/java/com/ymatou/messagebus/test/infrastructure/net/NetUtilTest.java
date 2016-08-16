/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.net;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;

public class NetUtilTest extends BaseTest {

    @Test
    @Ignore
    public void testGetHostIp() {
        String ip = NetUtil.getHostIp();

        System.out.println(NetUtil.getHostName());
        assertEquals("172.16.22.128", ip);
    }
}
