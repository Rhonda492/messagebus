/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 辅助工具类
 * 
 * @author wangxudong 2016年8月30日 下午3:14:04
 *
 */
public class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static InetAddress getInetAddress() {
        Collection<InetAddress> colInetAddress = getAllHostAddress();
        for (InetAddress address : colInetAddress) {
            if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                return address;
            }
        }
        return null;
    }

    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            logger.warn("get InetAddress failed because unknow host!", e);
            return new ArrayList<InetAddress>();
        }
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
