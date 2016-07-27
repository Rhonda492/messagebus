/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;

/**
 * @author wangxudong 2016年7月27日 下午7:14:02
 *
 */
@Component("publishMessageResource")
@Path("/message")
@Produces({"application/json; charset=UTF-8"})
@Consumes({MediaType.APPLICATION_JSON})
public class PublishMessageResourceImpl implements PublishMessageResource {

    @Autowired
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
    @Path("/publish")
    public String publish(PublishMessageReq req) {
        PublishMessageResp resp = publishMessageFacade.publish(req);

        return "ok";
    }

}
