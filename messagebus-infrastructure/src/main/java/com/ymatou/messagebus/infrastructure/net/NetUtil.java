/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络辅助类
 * 
 * @author wangxudong 2016年8月2日 上午11:40:56
 *
 */
public class NetUtil {

    private static Logger logger = LoggerFactory.getLogger(NetUtil.class);

    public static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("unknown host!");
            logger.warn("get InetAddress failed because unknow host!", e);
        }
        return null;
    }

    public static String getHostIp() {
        InetAddress inetAddress = getInetAddress();
        if (null == inetAddress) {
            return null;
        }
        String ip = inetAddress.getHostAddress();
        return ip;
    }

    public static String getHostName() {
        InetAddress inetAddress = getInetAddress();
        if (null == inetAddress) {
            return null;
        }
        String hostName = inetAddress.getHostName();
        return hostName;
    }
}
