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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.domain.service.DispatchService;
import com.ymatou.messagebus.facade.DispatchMessageFacade;
import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchMessageResp;
import com.ymatou.messagebus.facade.model.DispatchServerVO;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.infrastructure.rabbitmq.EndPoint;
import com.ymatou.messagebus.infrastructure.rabbitmq.MessageConsumer;
import com.ymatou.messagebus.infrastructure.thread.SemaphorManager;

@Component("dispatchResource")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DispatchResourceImpl implements DispatchResource {

    private static Logger logger = LoggerFactory.getLogger(DispatchResourceImpl.class);

    @Resource
    private DispatchConfig dispatchConfig;

    @Resource
    private DispatchMessageFacade dispatchMessageFacade;

    @Resource
    private DispatchService dispatchService;

    @GET
    @Path("/status")
    @Override
    public DispatchServerVO status() {
        DispatchServerVO dispatchServerInfo = new DispatchServerVO();
        dispatchServerInfo.setGroupId(dispatchConfig.getGroupId());
        dispatchServerInfo.setIp(NetUtil.getHostIp());
        dispatchServerInfo.setHostName(NetUtil.getHostName());
        dispatchServerInfo.setConsumerMap(MessageConsumer.getConsumerMap());
        dispatchServerInfo.setEndPointMap(EndPoint.getEndPointMap());
        dispatchServerInfo.setSemaphoreMap(SemaphorManager.getSemaphoreMap());

        return dispatchServerInfo;
    }

    @GET
    @Path("/stop")
    @Override
    public RestResp stop() {
        RestResp resp = new RestResp();
        try {
            dispatchService.stop();
            resp.setCode(200);

        } catch (Exception e) {
            resp.setCode(500);
            resp.setMessage(e.getMessage());
            logger.error("dispatch stop fail.", e);
        }
        return resp;
    }

    @GET
    @Path("/start")
    @Override
    public RestResp start() {
        RestResp resp = new RestResp();
        try {
            dispatchService.start();
            resp.setCode(200);

        } catch (Exception e) {
            resp.setCode(500);
            resp.setMessage(e.getMessage());
            logger.error("dispatch stop fail.", e);
        }
        return resp;
    }

    @Override
    @POST
    @Path("/{message:(?i:message)}/{dispatch:(?i:dispatch)}")
    public RestResp dispatch(DispatchMessageReq req) {
        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);

        return RestResp.newInstance(resp);
    }


}
