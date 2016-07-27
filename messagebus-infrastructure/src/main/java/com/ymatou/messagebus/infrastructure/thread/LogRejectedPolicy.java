/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程池对拒绝任务（无线程可用）的处理策略 记录日志
 * 
 * @author luoshiqian 2016/5/18 10:27
 */
public class LogRejectedPolicy implements RejectedExecutionHandler {

    private Logger logger = LoggerFactory.getLogger(LogRejectedPolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.error("Task " + r.toString() + " rejected from " + executor.toString());
    }
}
