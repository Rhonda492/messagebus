/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;


/**
 * 系统消息实现
 * 
 * @author wangxudong 2016年5月22日 下午10:09:34
 *
 */
@Component("systemResource")
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class SystemResourceImpl implements SystemResource {

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public String version() {
        return "{"
                + "\"1.0.0\":\"2016-09-01.1 first deploy.\","
                + "\"1.0.1\":\"2016-10-13.1 add kafka publish service.\","
                + "\"1.0.2\":\"2016-10-13.1 add kafka dispatch service.\","
                + "\"1.0.3\":\"2016-10-19.7 add kafka client sdk.\","
                + "\"1.0.4\":\"2016-10-24.1 add enableLog property to controll mongoDB log.\","
                + "\"1.0.5\":\"2016-10-28.3 report to monitor with appid_code.\""
                + "}";
    }


    @Override
    @GET
    @Path("/warmup")
    public String status() {

        return "ok";
    }
}
