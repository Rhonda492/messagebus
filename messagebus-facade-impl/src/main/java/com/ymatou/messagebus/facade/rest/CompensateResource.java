/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.model.CompensateReq;
import com.ymatou.messagebus.facade.model.ListLockResp;

/**
 * 补单站 REST API
 * 
 * @author tony 2016年8月14日 下午2:22:49
 *
 */
public interface CompensateResource {

    /**
     * 列出分布式锁
     * 
     * @return
     */
    public ListLockResp listLock();

    /**
     * 删除分布式锁
     * 
     * @param lockType
     * @return
     */
    public RestResp deleteLock(String lockType);

    /**
     * 补单
     * 
     * @param req
     * @return
     */
    public RestResp compensate(CompensateReq req);
}
