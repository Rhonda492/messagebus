/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import com.ymatou.messagebus.domain.util.CallbackSemaphoreHelper;
import org.junit.Test;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.model.MessageStatus;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.domain.repository.MessageStatusRepository;
import com.ymatou.messagebus.domain.service.CompensateService;
import com.ymatou.messagebus.facade.CompensateFacade;
import com.ymatou.messagebus.facade.enums.CallbackModeEnum;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageProcessStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusEnum;
import com.ymatou.messagebus.facade.model.CheckToCompensateReq;
import com.ymatou.messagebus.facade.model.CheckToCompensateResp;
import com.ymatou.messagebus.facade.model.CompensateReq;
import com.ymatou.messagebus.facade.model.CompensateResp;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.TaskItemRequest;

/**
 * 补单站 API测试
 * 
 * @author wangxudong 2016年8月15日 下午1:55:10
 *
 */
public class CompensateFacadeTest extends BaseTest {

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private CompensateFacade compensateFacade;

    @Resource
    private CompensateService compensateService;

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private MessageStatusRepository messageStatusRepository;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    private Message buildMessage(String appId, String code, String body) {
        Message message = new Message();
        message.setAppId(appId);
        message.setCode(code);
        message.setBody(body);
        message.setMessageId(UUID.randomUUID().toString());
        message.setUuid(Message.newUuid());
        message.setNewStatus(MessageNewStatusEnum.InRabbitMQ.code());
        message.setProcessStatus(MessageProcessStatusEnum.Init.code());
        message.setCreateTime(new Date());

        return message;
    }

    @Test
    public void testBoolean() {
        Boolean val = null;
        assertEquals(false, Boolean.FALSE.equals(val));

        val = true;
        assertEquals(false, Boolean.FALSE.equals(val));

        val = false;
        assertEquals(true, Boolean.FALSE.equals(val));
    }

    @Test
    public void testCheckToCompensate() {
        String appId = "testjava";
        String code = "hello";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -11);

        Message message = buildMessage(appId, code, "hello");
        message.setCreateTime(calendar.getTime());
        messageRepository.insert(message);

        CheckToCompensateReq req = new CheckToCompensateReq();
        req.setAppId(appId);
        req.setCode(code);

        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);
        assertEquals(true, resp.isSuccess());

        Message msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());

        MessageCompensate compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.NotRetry.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());
    }

    /**
     * 由于消息表是异步写入的，写入时间可能晚于分发时间，因此可能出现已经分发的消息没有改变状态，为避免这样的消息检测进入补单，在检测进入补单的逻辑里增加了对消息分发记录的查询判断
     * 
     * 如果发现需要检测进入补单的消息已经有过分发记录了，则不进入补单库，直接根据分发记录修改消息状态
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCheckToCompensateWhenMessageDispatched() throws InterruptedException {
        String appId = "testjava";
        String code = "hello";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -11);

        Message message = buildMessage(appId, code, "hello");
        message.setCreateTime(calendar.getTime());
        messageRepository.insert(message);

        // 模拟分发记录
        AppConfig appConfig = appConfigRepository.getAppConfig(appId);
        MessageConfig messageConfig = appConfig.getMessageConfig(code);
        String consumerId = messageConfig.getCallbackCfgList().get(0).getCallbackKey();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setMessageUuid(message.getUuid());
        messageStatus.setConsumerId(consumerId);
        messageStatus.setStatus(MessageStatusEnum.PushOk.toString());
        messageStatus.setCreateTime(new Date());
        messageStatusRepository.insert(messageStatus, appId);

        Thread.sleep(200);

        CheckToCompensateReq req = new CheckToCompensateReq();
        req.setAppId(appId);
        req.setCode(code);

        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);
        assertEquals(true, resp.isSuccess());

        Message msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.InRabbitMQ.code(), msgAssert.getNewStatus());
        assertEquals(MessageProcessStatusEnum.Success.code(), msgAssert.getProcessStatus());

        // 这种情况下不需要进入补单
        MessageCompensate compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNull(compensate);
    }

    @Test
    public void testCompensate() throws InterruptedException {
        String appId = "testjava";
        String code = "hello";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -11);

        Message message = buildMessage(appId, code, "hello");
        message.setCreateTime(calendar.getTime());
        messageRepository.insert(message);

        CheckToCompensateReq req = new CheckToCompensateReq();
        req.setAppId(appId);
        req.setCode(code);

        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);
        assertEquals(true, resp.isSuccess());

        Message msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());

        MessageCompensate compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.NotRetry.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());

        // 确保执行补单可以查出这条记录
        compensate.setRetryTime(new Date());
        messageCompensateRepository.update(compensate);

        // 执行补单
        CallbackSemaphoreHelper.initSemaphores();

        CompensateReq compensateReq = new CompensateReq();
        compensateReq.setAppId(appId);
        compensateReq.setCode(code);

        CompensateResp compensateResp = compensateFacade.compensate(compensateReq);
        assertEquals(true, compensateResp.isSuccess());

        Thread.sleep(1000);

        compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.Retrying.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());

        msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());
        assertEquals(MessageProcessStatusEnum.Compensate.code(), msgAssert.getProcessStatus());

        MessageStatus messageStatus = messageStatusRepository.getByUuid(appId, message.getUuid(), "testjava_hello_c0");
        assertNotNull(messageStatus);
        assertEquals(CallbackModeEnum.Compensate.toString(), messageStatus.getSource());
        assertEquals(true, messageStatus.getResult().startsWith("fail"));
    }

    @Test
    public void testCompensateSuccess() throws InterruptedException {
        String appId = "testjava";
        String code = "hello";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -11);

        Message message = buildMessage(appId, code, TaskItemRequest.newInstance().toString());
        message.setCreateTime(calendar.getTime());
        messageRepository.insert(message);

        CheckToCompensateReq req = new CheckToCompensateReq();
        req.setAppId(appId);
        req.setCode(code);

        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);
        assertEquals(true, resp.isSuccess());

        Message msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());

        MessageCompensate compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.NotRetry.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());
        assertEquals(0, compensate.getCompensateCount().intValue());

        System.out.println("MessageId:" + message.getMessageId());

        Calendar calandar = Calendar.getInstance();
        calandar.add(Calendar.MINUTE, 1);
        assertEquals(true, Math.abs(calandar.getTime().getTime() - compensate.getRetryTime().getTime()) < 1000);

        // 确保执行补单可以查出这条记录
        compensate.setRetryTime(new Date());
        messageCompensateRepository.update(compensate);

        // 执行补单
        CallbackSemaphoreHelper.initSemaphores();

        CompensateReq compensateReq = new CompensateReq();
        compensateReq.setAppId(appId);
        compensateReq.setCode(code);

        CompensateResp compensateResp = compensateFacade.compensate(compensateReq);
        assertEquals(true, compensateResp.isSuccess());

        Thread.sleep(1000);

        compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.RetryOk.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());

        msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());
        assertEquals(MessageProcessStatusEnum.Success.code(), msgAssert.getProcessStatus());

        MessageStatus messageStatus = messageStatusRepository.getByUuid(appId, message.getUuid(), "testjava_hello_c0");
        assertNotNull(messageStatus);
        assertEquals(CallbackModeEnum.Compensate.toString(), messageStatus.getSource());
        assertEquals(true, messageStatus.getResult().startsWith("ok"));
    }

    @Test
    public void testCompensateSuccessWithNoMessage() throws InterruptedException {
        String appId = "testjava";
        String code = "hello";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -11);

        Message message = buildMessage(appId, code, TaskItemRequest.newInstance().toString());
        message.setCreateTime(calendar.getTime());
        messageRepository.insert(message);

        CheckToCompensateReq req = new CheckToCompensateReq();
        req.setAppId(appId);
        req.setCode(code);

        CheckToCompensateResp resp = compensateFacade.checkToCompensate(req);
        assertEquals(true, resp.isSuccess());

        Message msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(msgAssert);
        assertEquals(MessageNewStatusEnum.CheckToCompensate.code(), msgAssert.getNewStatus());

        MessageCompensate compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.NotRetry.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());

        // 确保执行补单可以查出这条记录
        compensate.setRetryTime(new Date());
        messageCompensateRepository.update(compensate);

        // 删除消息，模拟MongoDB宕机，导致消息没有进入的场景
        messageRepository.delete(msgAssert);
        msgAssert = messageRepository.getByUuid(appId, code, message.getUuid());
        assertNull(msgAssert);


        // 执行补单
        CallbackSemaphoreHelper.initSemaphores();

        CompensateReq compensateReq = new CompensateReq();
        compensateReq.setAppId(appId);
        compensateReq.setCode(code);

        CompensateResp compensateResp = compensateFacade.compensate(compensateReq);
        assertEquals(true, compensateResp.isSuccess());

        Thread.sleep(1000);

        compensate = messageCompensateRepository.getByUuid(appId, code, message.getUuid());
        assertNotNull(compensate);
        assertEquals(MessageCompensateStatusEnum.RetryOk.code(), compensate.getNewStatus());
        assertEquals(MessageCompensateSourceEnum.Compensate.code(), compensate.getSource());

        MessageStatus messageStatus = messageStatusRepository.getByUuid(appId, message.getUuid(), "testjava_hello_c0");
        assertNotNull(messageStatus);
        assertEquals(CallbackModeEnum.Compensate.toString(), messageStatus.getSource());
        assertEquals(true, messageStatus.getResult().startsWith("ok"));
    }
}
