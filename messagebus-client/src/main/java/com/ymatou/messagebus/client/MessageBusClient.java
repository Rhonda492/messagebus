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
     */
    public boolean sendMessasge(PublishMessageReq req) {
        logger.debug("messagebus client send message:{}", req);

        PublishMessageResp resp = publishMessageFacade.publish(req);

        logger.debug("messagebus client recv response:{}", resp);

        return resp.isSuccess();
    }
}
