/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.task;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息补偿定时任务
 * 
 * @author wangxudong 2016年7月27日 下午4:56:26
 *
 */
public class MessageCompensateTask extends TimerTask {

    private static Logger logger = LoggerFactory.getLogger(MessageCompensateTask.class);

    @Override
    public void run() {
        logger.info("message compensate task run.");

    }

}
