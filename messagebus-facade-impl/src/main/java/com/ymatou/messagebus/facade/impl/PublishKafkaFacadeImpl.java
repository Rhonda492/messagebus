/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.service.KafkaBusService;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PublishKafkaFacade;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.enums.MessagePublishStatusEnum;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;

/**
 * 发布消息到Kafka接口实现
 * 
 * @author wangxudong 2016年7月27日 下午7:08:37
 *
 */
@Component("publishKafkaFacade")
public class PublishKafkaFacadeImpl implements PublishKafkaFacade {

    private static Logger logger = LoggerFactory.getLogger(PublishKafkaFacadeImpl.class);

    @Resource
    private KafkaBusService kafkaBusService;

    private String monitorAppId = "mqmonitor.iapi.ymatou.com";

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
        if (req == null) {
            logger.error("Recv: null");
            return builErrorResponse(ErrorCode.ILLEGAL_ARGUMENT, "request is null");
        }

        MDC.put("logPrefix", req.getMsgUniqueId());
        logger.info("Recv:{}", req);

        PublishMessageResp resp = null;
        try {
            req.validateData();
            resp = publishInner(req);

        } catch (IllegalArgumentException e) {
            resp = builErrorResponse(ErrorCode.ILLEGAL_ARGUMENT, e.getLocalizedMessage());
            logger.error("Invalid request: {}", req, e);

        } catch (BizException e) {
            resp = builErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage() + "|" +
                    e.getLocalizedMessage());
            logger.warn("Failed to execute request: {}, Error:{}", req.getRequestId(),
                    e.getErrorCode() + "|" + e.getErrorCode().getMessage() + "|" + e.getLocalizedMessage());

        } catch (Throwable e) {
            resp = builErrorResponse(ErrorCode.UNKNOWN, e.getLocalizedMessage());
            logger.error("Unknown error in executing request:{}", req, e);
        } finally {
            logger.info("Resp:{}", resp);
        }

        // 向性能监控器汇报性能情况
        long consumedTime = System.currentTimeMillis() - startTime;
        PerformanceStatisticContainer.addAsync(consumedTime, String.format("%s_%s", req.getAppId(), req.getCode()),
                monitorAppId);
        PerformanceStatisticContainer.addAsync(consumedTime, "TotalPublish", monitorAppId);


        return resp;
    }

    private PublishMessageResp publishInner(PublishMessageReq req) {
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

        kafkaBusService.publish(message);

        PublishMessageResp resp = new PublishMessageResp();
        resp.setSuccess(true);

        return resp;
    }



    private PublishMessageResp builErrorResponse(ErrorCode errorCode, String errorMsg) {
        PublishMessageResp resp = new PublishMessageResp();
        resp.setErrorCode(errorCode);
        resp.setErrorMessage(errorMsg);
        return resp;

    }

}
