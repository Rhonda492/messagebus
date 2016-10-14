/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishKafkaFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * Kafka消息本地消费者
 * 
 * @author wangxudong 2016年9月1日 下午12:04:20
 *
 */
public class KafkaLocalConsumer extends Thread {

    private static Logger logger = LoggerFactory.getLogger(KafkaLocalConsumer.class);

    /**
     * 消息数据库
     */
    private MessageDB messageDB;

    /**
     * 总线消息发布API
     */
    private PublishKafkaFacade publishKafkaFacade;

    /**
     * 构造函数
     * 
     * @param publishKafkaFacade
     */
    public KafkaLocalConsumer(PublishKafkaFacade publishKafkaFacade) {
        this.publishKafkaFacade = publishKafkaFacade;
    }


    @Override
    public void run() {
        try {
            while (true) {
                try {
                    consume();
                } catch (Throwable t) {
                    logger.error("kafka fail to consume local message.", t);
                }
                TimeUnit.MILLISECONDS.sleep(1000 * 5);
            }
        } catch (InterruptedException e) {
            logger.error("kafka message local consume thread is interrupted", e);
        }
    }

    /**
     * 消费消息
     */
    private void consume() {
        Iterator<String> keysIterator = messageDB.getKeysIterator(Constant.KAFKA_MAPNAME);
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            try {
                PublishMessageReq req = messageDB.get(Constant.KAFKA_MAPNAME, key);
                if (req == null) {
                    continue;
                }

                logger.debug("kafka messagebus client consumer send message:{}", req);

                PublishMessageResp resp = publishKafkaFacade.publish(req);

                logger.debug("kafka messagebus client consumer recv response:{}", resp);

                if (resp.isSuccess()) {
                    messageDB.delete(Constant.KAFKA_MAPNAME, key);
                } else {
                    if (ErrorCode.ILLEGAL_ARGUMENT.equals(resp.getErrorCode())) {
                        logger.error("kafka message local consume fail, will be remove from local db, cause:{}",
                                resp.getErrorMessage());
                        messageDB.delete(Constant.KAFKA_MAPNAME, key);
                    } else {
                        logger.error("kafka message local consume fail:{}", resp.getErrorMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("kafka consume message fail, key:" + key, e);
            }
        }

    }

    /**
     * @return the messageDB
     */
    public MessageDB getMessageDB() {
        return messageDB;
    }

    /**
     * @param messageDB the messageDB to set
     */
    public void setMessageDB(MessageDB messageDB) {
        this.messageDB = messageDB;
    }
}
