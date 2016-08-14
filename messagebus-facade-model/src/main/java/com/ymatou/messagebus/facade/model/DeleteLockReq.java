/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseRequest;

public class DeleteLockReq extends BaseRequest {

    private static final long serialVersionUID = 7605980931975675849L;

    /**
     * 锁类型
     */
    private String lockType;

    public String getLockType() {
        return lockType;
    }

    public void setLockType(String lockType) {
        this.lockType = lockType;
    }
}
