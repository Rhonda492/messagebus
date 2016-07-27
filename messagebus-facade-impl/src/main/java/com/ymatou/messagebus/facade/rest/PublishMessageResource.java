/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.model.PublishMessageReq;

public interface PublishMessageResource {

    /**
     * 发布单条消息
     * 
     * @param req
     * @return
     */
    public String publish(PublishMessageReq req);
}
