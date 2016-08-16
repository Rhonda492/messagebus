/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.Alarm;
import com.ymatou.messagebus.domain.repository.AlarmRepository;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月10日 下午2:12:37
 *
 */
public class AlarmRepositoryTest extends BaseTest {

    @Resource
    private AlarmRepository alarmRepository;

    @Test
    public void testGetByConsumerId() {
        String consumerId = "testjava_hello_c0";
        Alarm alarm = alarmRepository.getByConsumerId(consumerId);

        assertEquals("timedtask.ymatou.com", alarm.getAlarmAppId());
    }
}
