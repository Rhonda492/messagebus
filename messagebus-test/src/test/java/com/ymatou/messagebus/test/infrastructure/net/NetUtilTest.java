/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.net;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;

public class NetUtilTest extends BaseTest {

    @Test
    public void testGetHostIp() {
        String ip = NetUtil.getHostIp();

        System.out.println(ip);
        assertEquals(false, ip.startsWith("127"));
    }

    @Test
    public void testGetHostName() {
        String hostName = NetUtil.getHostName();

        System.out.println(hostName);
        assertEquals(true, hostName.contains("ymt.corp"));

    }
}

