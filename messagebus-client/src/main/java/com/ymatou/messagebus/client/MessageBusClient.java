/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * 消息总线客户端
 * 
 * @author wangxudong 2016年8月30日 上午11:51:35
 * 
 *         1.0.3-优化获取本机IP的性能
 *         1.0.4-修复MapDB关闭BUG
 *         1.0.5-修改sendMessage方法名，修复补单BUG
 *         1.0.6-修复MessageLocalConsumer中对于Facade放回的处理
 *         1.0.8 增加Component注解
 *
 */
public class MessageBusClient implements InitializingBean, DisposableBean {

    private Logger logger = LoggerFactory.getLogger(MessageBusClient.class);


    /**
     * 消息存储路径
     */
    private String messageDbPath;

    /**
     * 消息存储
     */
    private MessageDB messageDB;

    /**
     * 消息本地线程
     */
    private MessageLocalConsumer messageLocalConsumer;

    @Resource(name = "publishMessageClient")
    private PublishMessageFacade publishMessageFacade;

    /**
     * 发送消息
     * 
     * @param req
     * @return
     * @throws MessageBusException
     */
    public void sendMessage(Message message) throws MessageBusException {
        logger.debug("messagebus client send begin:{}", message);
        PublishMessageReq req = message.validateToReq();
        logger.debug("messagebus client send message:{}", req);

        try {
            PublishMessageResp resp = publishMessageFacade.publish(req);
            logger.debug("messagebus client recv response:{}", resp);

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
            logger.error("message bus send message fail.", e);
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
            messageDB.save(Constant.RABBITMQ_MAPNAME, key, messageReq);
        } catch (Exception ex) {
            logger.warn("publish message local fail, messageId:" + messageReq.getMsgUniqueId(), ex);
            throw new MessageBusException("publish message local fail, messageId:" + messageReq.getMsgUniqueId(), ex);
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
        messageDB = new MessageDB(getMessageDbPath(), Constant.RABBITMQ_MAPNAME);

        messageLocalConsumer = new MessageLocalConsumer(publishMessageFacade);
        messageLocalConsumer.setMessageDB(messageDB);
        messageLocalConsumer.setDaemon(true);
        messageLocalConsumer.start();

        logger.info("message bus client initialization success, version:{}, ip:{}, hostName:{}.", Constant.VERSION,
                NetUtil.getHostIp(), NetUtil.getHostName());
    }

    @Override
    public void destroy() throws Exception {
        if (messageDB != null) {
            messageDB.close();
        }
    }
}
