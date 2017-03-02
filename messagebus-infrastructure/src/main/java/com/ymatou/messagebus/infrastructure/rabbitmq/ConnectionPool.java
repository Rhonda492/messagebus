/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.ymatou.messagebus.infrastructure.thread.ScheduledExecutorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 连接池管理
 * 
 * @author wangxudong 2016年8月3日 下午5:02:16
 *
 */
/**
 * @author wangxudong 2016年8月19日 上午11:09:02
 *
 */
public class ConnectionPool {

    private static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    /**
     * key:{uri},主备有不同的URI
     */
    private static HashMap<String, ConnectionPool> connPoolMap = new HashMap<String, ConnectionPool>();

    /**
     * 连接工厂
     */
    private ConnectionFactory factory;


    /**
     * 初始化连接数量 默认值 3
     */
    private final static int INIT_CONN_NUM = 3;

    /*
     * 每个连接的最大使用数 默认值 30
     */
    private final static int MAX_CHANNEL_NUM_PER_CONN = 30;

    // 初始化连接数量
    private int initConnNum = INIT_CONN_NUM;

    // 每个连接的最大使用数
    private int maxChannelNumPerConn = MAX_CHANNEL_NUM_PER_CONN;


    /**
     * 连接列表
     */
    private List<ConnectionInfo> connList;

    /**
     * 构造函数
     * 
     * @param uri
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     * @throws TimeoutException
     * @throws IOException
     */
    private ConnectionPool(
            String uri)
            throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
        connList = new ArrayList<ConnectionInfo>();

        factory = new ConnectionFactory();
        factory.setUri(uri);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setHeartbeatExecutor(ScheduledExecutorHelper.newScheduledThreadPool(3, "rabbitmq-heartbeat-thread|" + uri));
    }

    /**
     * 获取到连接池实例
     * 
     * @param uri
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     * @throws TimeoutException
     * @throws IOException
     */
    public static ConnectionPool newInstance(String uri)
            throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
        ConnectionPool connectionPool = connPoolMap.get(uri);
        if (connectionPool == null) {
            synchronized (connPoolMap) {
                if (connPoolMap.containsKey(uri)) {
                    connectionPool = connPoolMap.get(uri);
                } else {
                    connectionPool = new ConnectionPool(uri);
                    connPoolMap.put(uri, connectionPool);
                }
            }
        }
        return connectionPool;
    }

    /**
     * 清空所有连接池
     * 
     * @throws IOException
     */
    public static void clearAll() throws IOException {
        synchronized (connPoolMap) {
            Iterator<ConnectionPool> iterator = connPoolMap.values().iterator();
            while (iterator.hasNext()) {
                ConnectionPool connectionPool = iterator.next();
                connectionPool.clear();
            }
        }
    }

    /**
     * 清空连接池
     * 
     * @throws IOException
     */
    public void clear() throws IOException {
        Iterator<ConnectionInfo> iterator = connList.iterator();
        while (iterator.hasNext()) {
            ConnectionInfo connectionInfo = iterator.next();
            Connection connection = connectionInfo.getConnection();
            if (connection.isOpen()) {
                connection.close();
            }
            iterator.remove();
        }
    }

    /**
     * 初始化连接列表
     * 
     * @throws IOException
     * @throws TimeoutException
     */
    public void init() throws IOException, TimeoutException {
        synchronized (connList) {
            if (connList.isEmpty()) {
                for (int i = 0; i < initConnNum; i++) {
                    Connection connection = factory.newConnection();
                    connList.add(new ConnectionInfo(connection));
                }
            }
        }
    }

    /**
     * 获取使用最少的连接
     * 
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public Connection getConnection() throws IOException, TimeoutException {
        if (connList.isEmpty()) {
            logger.warn("---------------------connList isEmpty---------------------------");
            Connection connection = factory.newConnection();
            synchronized (connList) {
                connList.add(new ConnectionInfo(connection));
            }
            return connection;
        }

        // 获取连接池中Channel数量最小的连接
        ConnectionInfo connectionInfo = connList.stream()
                .sorted(Comparator.comparing(ConnectionInfo::getCount))
                .findFirst().get();

        int channelNum = connectionInfo.getCount();
        if (channelNum < maxChannelNumPerConn) {
            connectionInfo.incCount();
            return connectionInfo.getConnection();
        } else {
            Connection connection = factory.newConnection();
            synchronized (connList) {
                ConnectionInfo connInfo = new ConnectionInfo(connection);
                connInfo.incCount();
                connList.add(connInfo);
            }
            return connection;
        }
    }

    /**
     * 获取到连接列表
     * 
     * @return
     */
    public List<ConnectionInfo> getConnList() {
        return connList;
    }

    /**
     * @return the initConnNum
     */
    public int getInitConnNum() {
        return initConnNum;
    }

    /**
     * @param initConnNum the initConnNum to set
     */
    public void setInitConnNum(int initConnNum) {
        this.initConnNum = initConnNum;
    }

    /**
     * @return the maxChannelNumPerConn
     */
    public int getMaxChannelNumPerConn() {
        return maxChannelNumPerConn;
    }

    /**
     * @param maxChannelNumPerConn the maxChannelNumPerConn to set
     */
    public void setMaxChannelNumPerConn(int maxChannelNumPerConn) {
        this.maxChannelNumPerConn = maxChannelNumPerConn;
    }
}
