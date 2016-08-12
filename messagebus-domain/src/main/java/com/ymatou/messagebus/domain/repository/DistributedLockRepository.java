/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.repository;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.ymatou.messagebus.domain.model.DistributedLock;
import com.ymatou.messagebus.infrastructure.mongodb.MongoRepository;
import com.ymatou.messagebus.infrastructure.net.NetUtil;

/**
 * 分布式锁仓储类
 * 
 * @author wangxudong 2016年8月12日 下午7:14:59
 *
 */
@Component
public class DistributedLockRepository extends MongoRepository {

    @Resource(name = "configMongoClient")
    private MongoClient mongoClient;

    private final String dbName = "MQ_Configuration_201505";

    @Override
    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 获取到分布式锁
     * 
     * @param lockType
     * @param lifeTimeMinute
     * @return
     */
    public boolean AcquireLock(String lockType, int lifeTimeMinute) {
        try {
            // DistributedLock lock = getByLockType(lockType);
            // if (lock != null && lock.getDeadTime().after(new Date())) {
            // return false;
            // }
            //
            // DistributedLock distributedLock = new DistributedLock();
            // if (lock != null) {
            // distributedLock = lock;
            // }

            DistributedLock distributedLock = new DistributedLock();
            Calendar now = Calendar.getInstance();
            distributedLock.setCreateTime(now.getTime());

            now.add(Calendar.MINUTE, lifeTimeMinute);
            distributedLock.setDeadTime(now.getTime());
            distributedLock.setLockType(lockType);
            distributedLock.setIp(NetUtil.getHostIp());
            distributedLock.setHostName(NetUtil.getHostName());

            insertEntiy(dbName, distributedLock);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据锁类型获取到锁信息
     * 
     * @param lockType
     * @return
     */
    public DistributedLock getByLockType(String lockType) {
        return getEntity(DistributedLock.class, dbName, "lockType", lockType);
    }
}
