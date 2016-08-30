/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * 消息总线客户端
 * 
 * @author wangxudong 2016年8月30日 上午11:51:35
 *
 */
@Component
public class MessageBusClient {

    private Logger logger = LoggerFactory.getLogger(MessageBusClient.class);

    @Resource(name = "publishMessageClient")
    private PublishMessageFacade publishMessageFacade;

    /**
     * 发送消息
     * 
     * @param req
     * @return
     * @throws MessageBusException
     */
    public void sendMessasge(Message message) throws MessageBusException {
        PublishMessageReq req = message.validateToReq();
        logger.debug("messagebus client send message:{}", req);

        try {
            PublishMessageResp resp = publishMessageFacade.publish(req);
            logger.debug("messagebus client recv response:{}", resp);

            if (resp.isSuccess()) {
                return;
            }

            if (ErrorCode.ILLEGAL_ARGUMENT.equals(resp.getErrorCode())) {
                throw new MessageBusException(resp.getErrorMessage());
            } else {
                publishLocal(req);
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
    private void publishLocal(PublishMessageReq req) throws MessageBusException {

    }
}
