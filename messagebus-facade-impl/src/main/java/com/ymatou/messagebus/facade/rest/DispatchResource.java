/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchServerInfo;

/**
 * 消息分发API
 * 
 * @author wangxudong 2016年8月4日 下午6:51:36
 *
 */
public interface DispatchResource {
    /**
     * 显示分发服务器状态
     * 
     * @return
     */
    public DispatchServerInfo status();

    /**
     * 分发消息
     * 
     * @return
     */
    public RestResp dispatch(DispatchMessageReq req);
}
