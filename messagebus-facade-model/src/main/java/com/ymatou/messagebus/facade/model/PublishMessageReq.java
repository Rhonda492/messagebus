/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import com.ymatou.messagebus.facade.BaseRequest;

/**
 * 发布消息请求体
 * 
 * @author wangxudong 2016年7月27日 下午6:51:48
 *
 */
public class PublishMessageReq extends BaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 应用Id
     */
    @NotEmpty(message = "appId not empty")
    private String appId;


    /**
     * 业务代码
     */
    @NotEmpty(message = "code not empty")
    private String code;


    /**
     * 消息Id
     */
    @NotEmpty(message = "messageId not empty")
    private String msgUniqueId;


    /**
     * 客户端Ip
     */
    private String ip;

    /**
     * 业务消息体
     */
    @NotNull(message = "body not null")
    private String body;

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
     * @return the msgUniqueId
     */
    public String getMsgUniqueId() {
        return msgUniqueId;
    }

    /**
     * @param msgUniqueId the msgUniqueId to set
     */
    public void setMsgUniqueId(String msgUniqueId) {
        this.msgUniqueId = msgUniqueId;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
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

    public void validateData() {
        if (StringUtils.isEmpty(appId)) {
            throw new IllegalArgumentException("appId not empty");
        }
        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("code not empty");
        }
        if (StringUtils.isEmpty(msgUniqueId)) {
            throw new IllegalArgumentException("messageId not empty");
        }
        if (StringUtils.isEmpty(body)) {
            throw new IllegalArgumentException("body not empty");
        }
    }
}
