/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.servlet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.domain.service.KafkaDispatchService;


/**
 * 消息分发定时任务
 * 
 * @author wangxudong 2016年7月27日 下午4:48:31
 *
 */
public class KafkaDispatchServlet {
    private static Logger logger = LoggerFactory.getLogger(KafkaDispatchServlet.class);

    /**
     * 分发服务
     */
    @Resource
    private KafkaDispatchService kafkaDispatchService;


    @PostConstruct
    public void init() throws Exception {
        kafkaDispatchService.start();
    }

    @PreDestroy
    public void destroy() {
        try {
            kafkaDispatchService.stop();
        } catch (Exception e) {
            logger.error("KafkaDispatchServlet destroy fail", e);
        }
    }
}
