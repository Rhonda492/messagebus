/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.cluster;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ymatou.messagebus.infrastructure.cluster.AutoResetHealthProxy;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月19日 下午2:44:10
 *
 */
public class AutoResetHealthProxyTest extends BaseTest {

    @Test
    public void testHealth() throws InterruptedException {
        AutoResetHealthProxy autoResetHealthProxy = new AutoResetHealthProxy(10);

        assertEquals(true, autoResetHealthProxy.isHealth());

        autoResetHealthProxy.setBroken();
        assertEquals(false, autoResetHealthProxy.isHealth());

        Thread.sleep(12);
        assertEquals(true, autoResetHealthProxy.isHealth());

        autoResetHealthProxy.setAutoResetMSecond(5);
        autoResetHealthProxy.setBroken();
        assertEquals(false, autoResetHealthProxy.isHealth());

        Thread.sleep(7);
        assertEquals(true, autoResetHealthProxy.isHealth());
    }
}
