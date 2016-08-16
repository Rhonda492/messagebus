/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.logger;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ymatou.errorreporter.api.BufferedErrorReporter;

@Component
public class ErrorReportClient implements InitializingBean {

    @Resource(name = "bufferErrorReporter")
    private BufferedErrorReporter bufferedErrorReporter;


    public void report(String title, Throwable ex, String appId) {
        bufferedErrorReporter.report(title, ex, appId);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        bufferedErrorReporter.init();
    }
}
