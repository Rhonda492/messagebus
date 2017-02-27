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

import com.ymatou.messagebus.infrastructure.net.NetUtil;


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
                + "\"ip\":\"" + NetUtil.getHostIp() + "\","
                + "\"1.0.0\":\"2016-09-01.1 first deploy.\","
                + "\"1.0.1\":\"2016-10-13.1 add kafka publish service.\","
                + "\"1.0.2\":\"2016-10-13.1 add kafka dispatch service.\","
                + "\"1.0.3\":\"2016-10-19.7 add kafka client sdk.\","
                + "\"1.0.4\":\"2016-10-24.1 add enableLog property to controll mongoDB log.\","
                + "\"1.0.5\":\"2016-10-31.1 report to monitor with appid_code.\","
                + "\"1.0.6\":\"2016-11-01.1 change message and compensate mongodb.\","
                + "\"1.0.7\":\"2016-11-03.1 set checkToCompensate time to 10 and reduce dispatch log.\","
                + "\"1.0.8\":\"2016-11-04.1 remove monitor aop.\","
                + "\"1.0.9\":\"2016-11-05.1 upgrade performace monitor to ver 1.1.2.\","
                + "\"1.1.0\":\"2016-11-07.1 fix checkToCompensate when message dispatched.\","
                + "\"1.1.1\":\"2016-12-13.1 fix message status index cause by isolate message and log.\","
                + "\"1.1.2\":\"2016-12-15.1 add kafka consumer pool size and allocation executorService by topic.\","
                + "\"1.1.3\":\"2016-12-15.2 set secondCompensate num to 3 and increase taskExecutor maxPoolSize to 100.\","
                + "\"1.1.4\":\"2017-02-13.2 compensate with timer one by one code.\""
                + "\"1.1.5\":\"2017-02-23.1 kafka optimization.\""
                + "\"1.1.6\":\"2017-02-27.1 callbackconfig remove hashcode,semaphor modify ConcurrentMap and null prevent \""
                + "}";
    }


    @Override
    @GET
    @Path("/warmup")
    public String status() {

        return "ok";
    }
}
