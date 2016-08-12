/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.repository;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.test.BaseTest;

public class AppConfigRepositoryTest extends BaseTest {

    @Resource
    AppConfigRepository appConfigRepository;



    @Test
    public void testAppConfig() {
        AppConfig appConfig = appConfigRepository.getAppConfig("testjava");

        assertNotNull(appConfig);
    }

    @Test
    public void testGetAllAppConfig() {
        List<AppConfig> appConfigs = appConfigRepository.getAllAppConfig();

        assertEquals(true, appConfigs.size() > 0);
    }
}
