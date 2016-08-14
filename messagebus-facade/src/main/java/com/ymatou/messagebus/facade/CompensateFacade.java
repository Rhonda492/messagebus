/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade;

import com.ymatou.messagebus.facade.model.CheckToCompensateReq;
import com.ymatou.messagebus.facade.model.CheckToCompensateResp;
import com.ymatou.messagebus.facade.model.DeleteLockReq;
import com.ymatou.messagebus.facade.model.DeleteLockResp;
import com.ymatou.messagebus.facade.model.ListLockReq;
import com.ymatou.messagebus.facade.model.ListLockResp;

/**
 * 补单站 API
 * 
 * @author tony 2016年8月14日 下午1:47:03
 *
 */
public interface CompensateFacade {

    /**
     * 显示分布式锁的列表
     * 
     * @param req
     * @return
     */
    public ListLockResp listLock(ListLockReq req);

    /**
     * 删除指定类型的锁
     * 
     * @param lockType
     */
    public DeleteLockResp deleteLock(DeleteLockReq req);

    /**
     * 检测出需要补偿的消息写入补单库
     * 
     * @param req
     * @return
     */
    public CheckToCompensateResp checkToCompensate(CheckToCompensateReq req);
}
