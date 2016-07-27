/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * 发布消息接口实现
 * 
 * @author wangxudong 2016年7月27日 下午7:08:37
 *
 */
@Component
public class PublishMessageFacadeImpl implements PublishMessageFacade {

    private static final Logger logger = LoggerFactory.getLogger(PublishMessageFacadeImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.PublishMessageFacade#publish(com.ymatou.messagebus.facade.model.
     * PublishMessageReq)
     */
    @Override
    public PublishMessageResp publish(PublishMessageReq req) {
        PublishMessageResp resp = new PublishMessageResp();
        resp.setSuccess(true);

        logger.info("publish message facade executed.");

        return resp;
    }

}
