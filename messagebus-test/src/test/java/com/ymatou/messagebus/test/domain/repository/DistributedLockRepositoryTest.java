/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.repository.DistributedLockRepository;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月12日 下午7:13:05
 *
 */
public class DistributedLockRepositoryTest extends BaseTest {

    @Resource
    private DistributedLockRepository distributedLockRepository;

    @Test
    public void testAcquireLock() {
        String lockType = "Compensate";
        distributedLockRepository.AcquireLock(lockType, 1);
    }
}
