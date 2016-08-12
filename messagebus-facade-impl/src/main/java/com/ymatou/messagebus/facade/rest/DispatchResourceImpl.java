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

import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.config.DispatchConfig;
import com.ymatou.messagebus.facade.DispatchMessageFacade;
import com.ymatou.messagebus.facade.model.DispatchMessageReq;
import com.ymatou.messagebus.facade.model.DispatchMessageResp;
import com.ymatou.messagebus.facade.model.DispatchServerInfo;
import com.ymatou.messagebus.infrastructure.net.NetUtil;

@Component("dispatchResource")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DispatchResourceImpl implements DispatchResource {

    @Resource
    private DispatchConfig dispatchConfig;

    @Resource
    private DispatchMessageFacade dispatchMessageFacade;

    @GET
    @Path("/status")
    @Override
    public DispatchServerInfo status() {
        DispatchServerInfo dispatchServerInfo = new DispatchServerInfo();
        dispatchServerInfo.setGroupId(dispatchConfig.getGroupId());
        dispatchServerInfo.setIp(NetUtil.getHostIp());
        dispatchServerInfo.setHostName(NetUtil.getHostName());

        return dispatchServerInfo;
    }

    @Override
    @POST
    @Path("/{message:(?i:message)}/{dispatch:(?i:dispatch)}")
    public RestResp dispatch(DispatchMessageReq req) {
        DispatchMessageResp resp = dispatchMessageFacade.dipatch(req);

        return RestResp.newInstance(resp);
    }


}
