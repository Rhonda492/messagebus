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
import com.ymatou.messagebus.infrastructure.net.NetUtil;

@DisconfUpdateService(confFileKeys = {"kafkaproducer.properties"})
@Component
public class KafkaProducerConfig extends Properties implements IDisconfUpdate, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerConfig.class);

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
            logger.error("reload kafkaproducer.properties failed.", e);
        } finally {
            myLock.writeLock().unlock();
        }
    }

    private void init() throws Exception {
        File mqProperties = DisConf.getLocalConfig("kafkaproducer.properties");

        load(new FileInputStream(mqProperties));
        put("client.id", "mqpublish.kafka." + NetUtil.getHostIp());

        logger.info("init kafkaproducer.properties:" + this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
