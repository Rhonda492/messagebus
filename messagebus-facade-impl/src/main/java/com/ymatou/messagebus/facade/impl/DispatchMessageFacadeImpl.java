/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.service.CallbackServiceImpl;
import com.ymatou.messagebus.facade.DispatchMessageFacade;
import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchMessageResp;

/**
 * 分发消息API实现
 * 
 * @author wangxudong 2016年8月12日 上午11:24:16
 *
 */
@Component
public class DispatchMessageFacadeImpl implements DispatchMessageFacade {

    @Resource
    private CallbackServiceImpl callbackServiceImpl;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.DispatchMessageFacade#dipatch(com.ymatou.messagebus.facade.model
     * .DispatchMessageReq)
     */
    @Override
    public DispatchMessageResp dipatch(DispatchMessageReq req) {
        DispatchMessageResp resp = new DispatchMessageResp();
        String queue = String.format("%s_%s", req.getAppId(), req.getCode());

        callbackServiceImpl.invoke(req.getAppId(), queue, req.getMessageBody(), req.getMessageId(),
                req.getMessageUuid());

        resp.setSuccess(true);

        return resp;
    }

}
