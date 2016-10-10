/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.model.PublishMessageRestReq;

public interface PublishKafkaResource {

    /**
     * 发布单条消息
     * 
     * @param req
     * @return
     */
    public RestResp publish(PublishMessageRestReq req);
}
