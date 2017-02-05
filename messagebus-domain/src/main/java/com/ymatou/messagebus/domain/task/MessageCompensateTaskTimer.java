/**
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.task;

import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.domain.model.MessageConfig;

/**
 * 补单任务调度器
 * 
 * @author Administrator 2017年2月5日 下午4:47:07
 *
 */
public class MessageCompensateTaskTimer {

    private static Logger logger = LoggerFactory.getLogger(MessageCompensateTask.class);

    private Timer timer;

    private MessageCompensateTask task;

    public MessageCompensateTaskTimer(String appId, MessageConfig messageConfig) {
        task = new MessageCompensateTask(appId, messageConfig);
    }

    public void setMessageConfig(MessageConfig messageConfig) {
        task.setMessageConfig(messageConfig);
    }

    public void start() {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(task, 0, 1000 * 30);

            logger.info("{} start success!", task.getTaskId());
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;

            logger.info("{} stop success!", task.getTaskId());
        }
    }
}
