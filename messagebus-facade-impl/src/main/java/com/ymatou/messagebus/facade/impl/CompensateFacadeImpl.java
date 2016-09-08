/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.DistributedLock;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.repository.DistributedLockRepository;
import com.ymatou.messagebus.domain.service.CompensateService;
import com.ymatou.messagebus.facade.CompensateFacade;
import com.ymatou.messagebus.facade.model.CheckToCompensateReq;
import com.ymatou.messagebus.facade.model.CheckToCompensateResp;
import com.ymatou.messagebus.facade.model.CompensateReq;
import com.ymatou.messagebus.facade.model.CompensateResp;
import com.ymatou.messagebus.facade.model.DeleteLockReq;
import com.ymatou.messagebus.facade.model.DeleteLockResp;
import com.ymatou.messagebus.facade.model.DistributedLockVO;
import com.ymatou.messagebus.facade.model.ListLockReq;
import com.ymatou.messagebus.facade.model.ListLockResp;
import com.ymatou.messagebus.facade.model.SecondCompensateReq;
import com.ymatou.messagebus.facade.model.SecondCompensateResp;

/**
 * 分发站 API实现
 * 
 * @author tony 2016年8月14日 下午1:58:01
 *
 */
@Component("compensateFacade")
public class CompensateFacadeImpl implements CompensateFacade {

    @Resource
    private DistributedLockRepository distributedLockRepository;

    @Resource
    private CompensateService compensateService;

    @Override
    public ListLockResp listLock(ListLockReq req) {
        List<DistributedLock> lockList = distributedLockRepository.getAll();
        List<DistributedLockVO> lockVOList = lockList.stream().map(this::fromModel).collect(Collectors.toList());

        ListLockResp listLockResp = new ListLockResp();
        listLockResp.setLockList(lockVOList);
        listLockResp.setSuccess(true);

        return listLockResp;
    }

    /**
     * 转换Model到VO
     * 
     * @param lock
     * @return
     */
    private DistributedLockVO fromModel(DistributedLock lock) {
        DistributedLockVO lockVO = new DistributedLockVO();
        lockVO.setCreateTime(lock.getCreateTime());
        lockVO.setDeadTime(lock.getDeadTime());
        lockVO.setHostName(lock.getHostName());
        lockVO.setIp(lock.getIp());
        lockVO.setLockType(lock.getLockType());

        return lockVO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.CompensateFacade#deleteLock(com.ymatou.messagebus.facade.model.
     * DeleteLockReq)
     */
    @Override
    public DeleteLockResp deleteLock(DeleteLockReq req) {
        distributedLockRepository.delete(req.getLockType());

        DeleteLockResp resp = new DeleteLockResp();
        resp.setSuccess(true);

        return resp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.CompensateFacade#checkToCompensate(com.ymatou.messagebus.facade.
     * model.CheckToCompensateReq)
     */
    @Override
    public CheckToCompensateResp checkToCompensate(CheckToCompensateReq req) {
        compensateService.checkToCompensate(req.getAppId(), req.getCode());

        CheckToCompensateResp resp = new CheckToCompensateResp();
        resp.setSuccess(true);
        return resp;
    }

    @Override
    public CompensateResp compensate(CompensateReq req) {
        compensateService.compensate(req.getAppId(), req.getCode());

        CompensateResp resp = new CompensateResp();
        resp.setSuccess(true);
        return resp;
    }

    @Override
    public SecondCompensateResp secondCompensate(SecondCompensateReq req) {
        Message message = new Message();
        message.setAppId(req.getAppId());
        message.setCode(req.getCode());
        message.setUuid(req.getUuid());
        message.setMessageId(req.getMessageId());
        message.setBody(req.getBody());

        compensateService.secondCompensate(message, req.getConsumerId(), req.getTimeSpanSecond().intValue());

        SecondCompensateResp resp = new SecondCompensateResp();
        resp.setSuccess(true);
        return resp;
    }
}
