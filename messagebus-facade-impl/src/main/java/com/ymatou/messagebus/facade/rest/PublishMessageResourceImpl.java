/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.facade.model.PublishMessageRestReq;

/**
 * @author wangxudong 2016年7月27日 下午7:14:02
 *
 */
@Component("publishMessageResource")
@Path("/{message:(?i:message)}")
@Produces({"application/json; charset=UTF-8"})
@Consumes({MediaType.APPLICATION_JSON})
public class PublishMessageResourceImpl implements PublishMessageResource {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Resource
    PublishMessageFacade publishMessageFacade;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.rest.PublishMessageResource#publish(com.ymatou.messagebus.facade
     * .model.PublishMessageReq)
     */
    @Override
    @POST
    @Path("/{publish:(?i:publish)}")
    public RestResp publish(PublishMessageRestReq req) {
        PublishMessageReq request = new PublishMessageReq();
        request.setAppId(req.getAppId());
        request.setCode(request.getCode());
        request.setIp(req.getIp());
        request.setMsgUniqueId(req.getMsgUniqueId());
        request.setBody(JSON.toJSONStringWithDateFormat(req.getBody(), DATE_FORMAT));

        PublishMessageResp resp = publishMessageFacade.publish(request);

        return RestResp.newInstance(resp);
    }

}
