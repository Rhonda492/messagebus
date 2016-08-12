/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade;

import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchMessageResp;

/**
 * 分发消息API
 * 
 * @author wangxudong 2016年8月12日 上午11:15:42
 *
 */
public interface DispatchMessageFacade {

    /**
     * 分发单条消息
     * 
     * @param req
     * @return
     */
    public DispatchMessageResp dipatch(DispatchMessageReq req);
}
