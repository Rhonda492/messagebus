/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.ymatou.messagebus.facade.PrintFriendliness;
import com.ymatou.messagebus.facade.model.PublishMessageReq;

/**
 * 总线消息
 * 
 * @author wangxudong 2016年8月30日 下午8:36:47
 *
 */
public class Message extends PrintFriendliness {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public Message() {}

    public Message(String appId, String code, String messageId, Object body) {
        this.appId = appId;
        this.code = code;
        this.messageId = messageId;
        this.body = body;
    }

    private static final long serialVersionUID = 815204437225169174L;

    /**
     * 应用代码
     */
    private String appId;


    /**
     * 业务代码
     */
    private String code;


    /**
     * 消息编号
     */
    private String messageId;

    /**
     * 消息体
     */
    private Object body;

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the body
     */
    public Object getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * 验证请求并转换成模型
     * 
     * @return
     * @throws MessageBusException
     */
    public PublishMessageReq validateToReq() throws MessageBusException {
        if (StringUtils.isEmpty(appId)) {
            throw new MessageBusException("invalid appId.");
        }

        if (StringUtils.isEmpty(code)) {
            throw new MessageBusException("invalid code.");
        }

        if (StringUtils.isEmpty(messageId)) {
            throw new MessageBusException("invalid message id.");
        }

        if (body == null) {
            throw new MessageBusException("body must not null");
        }

        PublishMessageReq publishMessageReq = new PublishMessageReq();
        publishMessageReq.setAppId(appId);
        publishMessageReq.setCode(code);
        publishMessageReq.setMsgUniqueId(messageId);
        publishMessageReq.setIp(NetUtil.getHostIp());
        publishMessageReq.setBody(JSON.toJSONStringWithDateFormat(body, DATE_FORMAT));

        return publishMessageReq;
    }
}
