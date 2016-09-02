/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.CompensateFacade;
import com.ymatou.messagebus.facade.model.CheckToCompensateReq;
import com.ymatou.messagebus.facade.model.CheckToCompensateResp;
import com.ymatou.messagebus.facade.model.CompensateReq;
import com.ymatou.messagebus.facade.model.CompensateResp;
import com.ymatou.messagebus.facade.model.DeleteLockReq;
import com.ymatou.messagebus.facade.model.DeleteLockResp;
import com.ymatou.messagebus.facade.model.ListLockReq;
import com.ymatou.messagebus.facade.model.ListLockResp;

/**
 * 补单站 REST 实现
 * 
 * @author tony 2016年8月14日 下午2:25:03
 *
 */
@Component("compensateResource")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class CompensateResourceImpl implements CompensateResource {

    private static Logger logger = LoggerFactory.getLogger(CompensateResourceImpl.class);

    @Resource
    private CompensateFacade compensateFacade;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    @GET
    @Path("/lock/list")
    @Override
    public ListLockResp listLock() {
        ListLockReq req = new ListLockReq();

        return compensateFacade.listLock(req);
    }

    @GET
    @Path("/lock/delete/{lockType}")
    @Override
    public RestResp deleteLock(@PathParam("lockType") String lockType) {
        DeleteLockReq req = new DeleteLockReq();
        req.setLockType(lockType);

        DeleteLockResp resp = compensateFacade.deleteLock(req);

        return RestResp.newInstance(resp);
    }


    @GET
    @Path("/index")
    @Override
    public String index() {
        logger.info("-------------------index start------------------------");
        try {
            messageCompensateRepository.index();
            logger.info("message compensate index success.");

            messageRepository.index();
            logger.info("message and status index success.");

            logger.info("-------------------index success------------------------");
        } catch (Exception exception) {
            logger.error("compensate index failed", exception);
            return exception.getMessage();
        }

        return "ok";
    }

    @POST
    @Path("/{message:(?i:message)}/{compensate:(?i:compensate)}")
    @Override
    public RestResp compensate(CompensateReq req) {
        CompensateResp resp = compensateFacade.compensate(req);

        return RestResp.newInstance(resp);
    }

    @POST
    @Path("/{message:(?i:message)}/{checkToCompensate:(?i:checkToCompensate)}")
    @Override
    public RestResp checkToCompensate(CheckToCompensateReq req) {
        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);

        return RestResp.newInstance(resp);
    }
}
