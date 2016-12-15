/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.baidu.disconf.client.DisConf;
import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;

@DisconfUpdateService(confFileKeys = {"kafkaconsumer.properties"})
@Component
public class KafkaConsumerConfig extends Properties implements IDisconfUpdate, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    /**
     * 
     */
    private static final long serialVersionUID = -3005463161045289417L;

    /**
     * 读写锁
     */
    private ReadWriteLock myLock = new ReentrantReadWriteLock();

    @Override
    public void reload() throws Exception {
        myLock.writeLock().lock();
        try {
            init();
        } catch (Exception e) {
            logger.error("reload kafkaconsumer.properties failed.", e);
        } finally {
            myLock.writeLock().unlock();
        }
    }

    private void init() throws Exception {
        File mqProperties = DisConf.getLocalConfig("kafkaconsumer.properties");

        load(new FileInputStream(mqProperties));
        put("enable.auto.commit", "true");

        logger.info("init kafkaconsumer.properties:" + this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    /**
     * 获取到消费者一次拉取消息的数量
     * 
     * @return
     */
    public int getConsumerPollSize() {
        String poolSize = getProperty("consumer.poll.size", "300");

        return Integer.valueOf(poolSize).intValue();
    }
}
