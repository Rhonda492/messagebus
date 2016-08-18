/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.task;

import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.ymatou.messagebus.domain.service.DispatchService;

/**
 * 消息分发定时任务
 * 
 * @author wangxudong 2016年7月27日 下午4:56:26
 *
 */
public class MessageDispatchTask extends TimerTask {

    private static Logger logger = LoggerFactory.getLogger(MessageDispatchTask.class);

    private DispatchService dispatchService;


    public MessageDispatchTask() {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        dispatchService = (DispatchService) wac.getBean("dispatchService");
    }

    @Override
    public void run() {
        try {
            MDC.put("logPrefix", "MessageDispatchTask|" + UUID.randomUUID().toString().replaceAll("-", ""));

            dispatchService.checkReload();
            logger.info("dispatch service check reload.");
        } catch (Exception e) {
            logger.error("dispatch service check reload failed.", e);
        }
    }
}
