/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.infrastructure.logger;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.infrastructure.logger.ErrorReportClient;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年8月10日 下午12:03:27
 *
 */
public class ErrorReportClientTest extends BaseTest {

    @Resource
    private ErrorReportClient errorReportClient;

    @Test
    public void testReport() throws InterruptedException {
        errorReportClient.report("test report", null, "payment.iapi.ymatou.com");
    }
}
