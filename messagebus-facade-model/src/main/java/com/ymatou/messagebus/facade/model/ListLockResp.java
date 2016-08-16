/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;


import java.util.List;

import com.ymatou.messagebus.facade.BaseResponse;

/**
 * 分布式锁 Resp
 * 
 * @author tony 2016年8月14日 下午1:50:13
 *
 */
public class ListLockResp extends BaseResponse {

    private static final long serialVersionUID = 1181806440055920360L;


    /**
     * 分布式锁列表
     */
    private List<DistributedLockVO> lockList;


    public List<DistributedLockVO> getLockList() {
        return lockList;
    }


    public void setLockList(List<DistributedLockVO> lockList) {
        this.lockList = lockList;
    }
}
