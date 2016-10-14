/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishKafkaFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * Kafka总线客户端
 * 
 * @author wangxudong 2016年10月14日 下午3:39:54
 * 
 *         1.0.7 增加Kafka总线客户端
 *
 */
public class KafkaBusClient implements InitializingBean, DisposableBean {

    private Logger logger = LoggerFactory.getLogger(KafkaBusClient.class);


    /**
     * 消息存储路径
     */
    private String messageDbPath;

    /**
     * 消息存储
     */
    private MessageDB messageDB;

    /**
     * 异步发送最大线程数
     */
    private int asyncSendMaxThreadNum = 1;

    /**
     * 异步发送队列最大长度
     */
    private int asyncSendMaxQueueNum = 10000;

    /**
     * Kafka总线线程池
     */
    private ExecutorService kafkaBusExecutor;

    /**
     * 消息本地线程
     */
    private KafkaLocalConsumer kafkaLocalConsumer;

    @Resource(name = "publishKafkaClient")
    private PublishKafkaFacade publishKafkaFacade;

    /**
     * 发送消息
     * 
     * @param req
     * @return
     * @throws MessageBusException
     */
    public void sendMessage(Message message) throws MessageBusException {
        logger.debug("kafka messagebus client send begin:{}", message);
        PublishMessageReq req = message.validateToReq();
        logger.debug("kafka messagebus client send message:{}", req);

        sendMessageInner(req);
    }

    /**
     * 发送消息
     * 
     * @param req
     * @return
     * @throws MessageBusException
     */
    public void sendMessageAsync(Message message) throws MessageBusException {
        logger.debug("kafka messagebus client send begin:{}", message);
        PublishMessageReq req = message.validateToReq();
        logger.debug("kafka messagebus client send message:{}", req);

        try {
            kafkaBusExecutor.submit(() -> {
                try {
                    sendMessageInner(req);
                } catch (Exception e) {
                    logger.error("kafka message bus send message fail,messageId:" + req.getMsgUniqueId(), e);
                }
            });
        } catch (Exception e) {
            logger.error("kafka client send message thread pool used up", e);
        }
    }

    /**
     * 发送消息内部实现
     * 
     * @param req
     * @throws MessageBusException
     */
    private void sendMessageInner(PublishMessageReq req) throws MessageBusException {
        try {
            PublishMessageResp resp = publishKafkaFacade.publish(req);
            logger.debug("kafka messagebus client recv response:{}", resp);

            if (!resp.isSuccess()) {
                if (ErrorCode.ILLEGAL_ARGUMENT.equals(resp.getErrorCode())) {
                    throw new MessageBusException(resp.getErrorMessage());
                } else {
                    publishLocal(req);
                }
            }
        } catch (MessageBusException busException) {
            throw busException;
        } catch (Exception e) {
            logger.error("kafka message bus send message fail.", e);
            publishLocal(req);
        }
    }



    /**
     * 发布消息到本地
     * 
     * @param req
     * @throws MessageBusException
     */
    private void publishLocal(PublishMessageReq messageReq) throws MessageBusException {
        try {
            String key = messageReq.getAppId() + messageReq.getCode() + messageReq.getMsgUniqueId();
            messageDB.save(Constant.KAFKA_MAPNAME, key, messageReq);
        } catch (Exception ex) {
            logger.warn("kafka publish message local fail, messageId:" + messageReq.getMsgUniqueId(), ex);
            throw new MessageBusException("kafka publish message local fail, messageId:" + messageReq.getMsgUniqueId(),
                    ex);
        }
    }

    /**
     * @return the messageDbPath
     */
    public String getMessageDbPath() {
        return messageDbPath;
    }

    /**
     * @param messageDbPath the messageDbPath to set
     */
    public void setMessageDbPath(String messageDbPath) {
        this.messageDbPath = messageDbPath;
    }

    /**
     * 获取到数据存储(测试专用)
     * 
     * @return
     */
    @SuppressWarnings("unused")
    private MessageDB getMessageDB() {
        return messageDB;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        messageDB = new MessageDB(getMessageDbPath(), Constant.KAFKA_MAPNAME);

        kafkaLocalConsumer = new KafkaLocalConsumer(publishKafkaFacade);
        kafkaLocalConsumer.setMessageDB(messageDB);
        kafkaLocalConsumer.setDaemon(true);
        kafkaLocalConsumer.start();

        kafkaBusExecutor = new ThreadPoolExecutor(1, getAsyncSendMaxThreadNum(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(getAsyncSendMaxQueueNum()));

        logger.info("kafka message bus client initialization success, version:{}, ip:{}, hostName:{}.",
                Constant.VERSION, NetUtil.getHostIp(), NetUtil.getHostName());
    }

    @Override
    public void destroy() throws Exception {
        if (messageDB != null) {
            messageDB.close();
        }
    }

    /**
     * @return the asyncSendThreadNum
     */
    public int getAsyncSendMaxThreadNum() {


        return asyncSendMaxThreadNum;
    }

    /**
     * @param asyncSendThreadNum the asyncSendThreadNum to set
     */
    public void setAsyncSendMaxThreadNum(int asyncSendThreadNum) {
        if (asyncSendMaxThreadNum <= 0 || asyncSendMaxThreadNum > 30) {
            this.asyncSendMaxThreadNum = 1;
        } else {
            this.asyncSendMaxThreadNum = asyncSendThreadNum;
        }
    }

    /**
     * @return the asyncSendMaxQueueNum
     */
    public int getAsyncSendMaxQueueNum() {
        return asyncSendMaxQueueNum;
    }

    /**
     * @param asyncSendMaxQueueNum the asyncSendMaxQueueNum to set
     */
    public void setAsyncSendMaxQueueNum(int asyncSendMaxQueueNum) {
        if (asyncSendMaxThreadNum <= 0) {
            this.asyncSendMaxQueueNum = 10000;
        } else {
            this.asyncSendMaxQueueNum = asyncSendMaxQueueNum;
        }
    }
}
