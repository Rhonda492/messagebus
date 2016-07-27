package com.ymatou.messagebus.facade;

import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * 发布消息接口
 * 
 * @author wangxudong 2016年7月27日 下午7:00:13
 *
 */
public interface PublishMessageFacade {

    /**
     * 发布单条消息
     * 
     * @param req
     * @return
     */
    public PublishMessageResp publish(PublishMessageReq req);
}
