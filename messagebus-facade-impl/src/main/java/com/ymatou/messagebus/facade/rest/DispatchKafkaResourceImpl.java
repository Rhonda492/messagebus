/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.service.KafkaDispatchService;


/**
 * 系统消息实现
 * 
 * @author wangxudong 2016年5月22日 下午10:09:34
 *
 */
@Component("dispatchKafkaResource")
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class DispatchKafkaResourceImpl implements DispatchKafkaResource {

    private static Logger logger = LoggerFactory.getLogger(DispatchKafkaResourceImpl.class);

    @Resource
    private KafkaDispatchService kafkaDispatchService;

    @Override
    @GET
    @Path("/start")
    public String start() {
        try {
            kafkaDispatchService.start();
            return "start success!";
        } catch (Exception e) {
            logger.error("start kafka dispatch service failed.", e);
            return "start fail, " + e.getMessage();
        }
    }

    @Override
    @GET
    @Path("/stop")
    public String stop() {
        try {
            kafkaDispatchService.stop();
            return "stop success!";
        } catch (Exception e) {
            logger.error("start kafka dispatch service failed.", e);
            return "stop fail, " + e.getMessage();
        }
    }
}
