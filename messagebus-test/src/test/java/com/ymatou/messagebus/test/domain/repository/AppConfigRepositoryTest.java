/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.repository.AppConfigRepository;
import com.ymatou.messagebus.test.BaseTest;

public class AppConfigRepositoryTest extends BaseTest {

    @Resource
    AppConfigRepository appConfigRepository;

    @Test
    public void testCount() {
        long count = appConfigRepository.count();

        assertEquals(26, count);
    }
}
