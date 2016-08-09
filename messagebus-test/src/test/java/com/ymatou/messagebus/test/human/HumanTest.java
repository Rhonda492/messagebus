/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.human;

import javax.annotation.Resource;

import org.junit.Test;

import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.messagebus.infrastructure.net.NetUtil;
import com.ymatou.messagebus.test.BaseTest;
import com.ymatou.messagebus.test.MessageBody;

/**
 * 人工测试
 * 
 * @author wangxudong 2016年8月9日 下午7:06:11
 *
 */
public class HumanTest extends BaseTest {

    @Resource
    private PublishMessageFacade publishMessageFacade;

    @Resource
    private MessageRepository messageRepository;


    @Test
    public void testPublishMessage() throws InterruptedException {
        PublishMessageReq req = new PublishMessageReq();
        req.setAppId("testjava");
        req.setCode("hello");
        req.setIp(NetUtil.getHostIp());


        for (int i = 0; i < 50; i++) {
            MessageBody messageBody = MessageBody.newInstance();
            req.setMsgUniqueId(messageBody.getBizId());
            req.setBody(messageBody);

            PublishMessageResp resp = publishMessageFacade.publish(req);
            System.out.println(String.format("isSuccess:%s, message:%s", resp.isSuccess(), resp.getErrorMessage()));
        }
    }
}
