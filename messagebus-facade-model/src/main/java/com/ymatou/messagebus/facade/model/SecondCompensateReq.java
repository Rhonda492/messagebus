/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseRequest;

/**
 * 秒级补单请求
 * 
 * @author wangxudong 2016年9月8日 下午6:36:05
 *
 */
public class SecondCompensateReq extends BaseRequest {
    private static final long serialVersionUID = 46142542533944078L;

    /**
     * 应用编号
     */
    private String appId;

    /**
     * 业务代码
     */
    private String code;

    /**
     * 消息UUID
     */
    private String uuid;

    /**
     * 消息Id
     */
    private String messageId;

    /**
     * 消息体
     */
    private String body;

    /**
     * 消费者Id
     */
    private String consumerId;

    /**
     * 秒级补单时间间隔
     */
    private Integer timeSpanSecond;

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
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the consumerId
     */
    public String getConsumerId() {
        return consumerId;
    }

    /**
     * @param consumerId the consumerId to set
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    /**
     * @return the timeSpanSecond
     */
    public Integer getTimeSpanSecond() {
        return timeSpanSecond;
    }

    /**
     * @param timeSpanSecond the timeSpanSecond to set
     */
    public void setTimeSpanSecond(Integer timeSpanSecond) {
        this.timeSpanSecond = timeSpanSecond;
    }
}
