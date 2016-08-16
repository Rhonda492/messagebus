package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.DistributedLock;
import com.ymatou.messagebus.domain.repository.DistributedLockRepository;
import com.ymatou.messagebus.test.BaseTest;



/**
 * @author tony 2016年8月14日 下午12:18:29
 *
 */
public class DistributedLockRepositoryTest extends BaseTest {

    @Resource
    private DistributedLockRepository distributedLockRepository;

    @Test
    public void testAcquireLock() {
        String lockType = "Compensate";
        distributedLockRepository.delete(lockType);
        distributedLockRepository.AcquireLock(lockType, 1);

        DistributedLock lock = distributedLockRepository.getByLockType(lockType);

        assertNotNull(lock);
        assertEquals(lockType, lock.getLockType());
    }
}
