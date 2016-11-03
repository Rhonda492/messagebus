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
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * 消息本地消费者
 * 
 * @author wangxudong 2016年9月1日 下午12:04:20
 *
 */
public class MessageLocalConsumer extends Thread {

    private static Logger logger = LoggerFactory.getLogger(MessageLocalConsumer.class);

    /**
     * 消息数据库
     */
    private MessageDB messageDB;

    /**
     * 总线消息发布API
     */
    private PublishMessageFacade publishMessageFacade;

    /**
     * 构造函数
     * 
     * @param publishMessageFacade
     */
    public MessageLocalConsumer(PublishMessageFacade publishMessageFacade) {
        this.publishMessageFacade = publishMessageFacade;
    }


    @Override
    public void run() {
        try {
            while (true) {
                try {
                    consume();
                } catch (IllegalAccessError error) {
                    throw error;
                } catch (Throwable t) {
                    logger.error("fail to consume local message.", t);
                }
                TimeUnit.MILLISECONDS.sleep(1000 * 5);
            }
        } catch (InterruptedException e) {
            logger.warn("message local consume thread is interrupted", e);
        }
    }

    /**
     * 消费消息
     */
    private void consume() {
        Iterator<String> keysIterator = messageDB.getKeysIterator(Constant.RABBITMQ_MAPNAME);
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            try {
                PublishMessageReq req = messageDB.get(Constant.RABBITMQ_MAPNAME, key);
                if (req == null) {
                    continue;
                }

                logger.debug("messagebus client consumer send message:{}", req);

                PublishMessageResp resp = publishMessageFacade.publish(req);

                logger.debug("messagebus client consumer recv response:{}", resp);

                if (resp.isSuccess()) {
                    messageDB.delete(Constant.RABBITMQ_MAPNAME, key);
                } else {
                    if (ErrorCode.ILLEGAL_ARGUMENT.equals(resp.getErrorCode())) {
                        logger.error("message local consume fail, will be remove from local db, cause:{}",
                                resp.getErrorMessage());
                        messageDB.delete(Constant.RABBITMQ_MAPNAME, key);
                    } else {
                        logger.error("message local consume fail:{}", resp.getErrorMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("consume message fail, key:" + key, e);
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
