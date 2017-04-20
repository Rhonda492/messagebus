/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.impl;

import java.util.Date;

import javax.annotation.Resource;

import com.ymatou.messagebus.domain.config.ForwardConfig;
import com.ymatou.messagebus.facade.ReceiveMessageFacade;
import com.ymatou.messagebus.facade.model.ReceiveMessageReq;
import com.ymatou.messagebus.facade.model.ReceiveMessageResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.service.MessageBusService;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.enums.MessagePublishStatusEnum;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;

/**
 * 发布消息接口实现
 * 
 * @author wangxudong 2016年7月27日 下午7:08:37
 *
 */
@Component("publishMessageFacade")
public class PublishMessageFacadeImpl implements PublishMessageFacade {

    private static Logger logger = LoggerFactory.getLogger(PublishMessageFacadeImpl.class);

    @Resource
    private MessageBusService messageBusService;

    @Autowired(required = false)
    private ReceiveMessageFacade receiveMessageFacade;

    @Resource
    private ForwardConfig forwardConfig;

    /**
     * 按照业务统计
     */
    private String monitorAppId = "mqmonitor.iapi.ymatou.com";

    /**
     * 按照机器统计性能数据
     */
    private String staticAppId = "mqstatic.iapi.ymatou.com";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ymatou.messagebus.facade.PublishMessageFacade#publish(com.ymatou.messagebus.facade.model.
     * PublishMessageReq)
     */
    @Override
    public PublishMessageResp publish(PublishMessageReq req) {
        long startTime = System.currentTimeMillis();

        PublishMessageResp resp;
        if(receiveMessageFacade != null && forwardConfig.isNeedForward(req.getCode())){
            resp = forwardToReceiver(req);
        }else {
            resp = selfHandle(req);
        }

        // 向性能监控器汇报性能情况
        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.add(consumedTime, req.getCode(), monitorAppId);
        PerformanceStatisticContainer.add(consumedTime, "TotalPublish", monitorAppId);
        PerformanceStatisticContainer.add(consumedTime, String.format("TotalPublish.%s", NetUtil.getHostIp()),
                staticAppId);

        return resp;
    }

    /**
     * 转发到新的接收站
     * @param req
     * @return
     */
    private PublishMessageResp forwardToReceiver(PublishMessageReq req){
        ReceiveMessageReq receiveMessageReq = new ReceiveMessageReq();
        BeanUtils.copyProperties(req,receiveMessageReq);

        ReceiveMessageResp resp = receiveMessageFacade.publish(receiveMessageReq);

        if(resp.isSuccess()){
            PublishMessageResp publishMessageResp = new PublishMessageResp();
            resp.setSuccess(true);
            resp.setUuid(resp.getUuid());
            return publishMessageResp;
        }else {
            return selfHandle(req);
        }
    }

    /**
     * 本站处理
     * @param req
     * @return
     */
    private PublishMessageResp selfHandle(PublishMessageReq req){
        Message message = new Message();
        message.setAppId(req.getAppId());
        message.setBody(req.getBody());
        message.setCode(req.getCode());
        message.setIp(req.getIp());
        message.setCreateTime(new Date());
        message.setBusReceivedServerIp(NetUtil.getHostIp());
        message.setPushStatus(MessagePublishStatusEnum.AlreadyPush.code()); // 避免被.NET版补单
        message.setUuid(Message.newUuid());
        message.setMessageId(req.getMsgUniqueId());
        message.setNewStatus(MessageNewStatusEnum.InRabbitMQ.code());
        message.setProcessStatus(MessageProcessStatusEnum.Init.code());

        messageBusService.publish(message);

        PublishMessageResp resp = new PublishMessageResp();
        resp.setSuccess(true);
        resp.setUuid(message.getUuid());
        return resp;
    }

}
