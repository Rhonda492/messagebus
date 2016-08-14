/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.model.ListLockResp;

/**
 * 补单站 REST API
 * 
 * @author tony 2016年8月14日 下午2:22:49
 *
 */
public interface CompensateResource {

    public ListLockResp listLock();

    public RestResp deleteLock(String lockType);
}
