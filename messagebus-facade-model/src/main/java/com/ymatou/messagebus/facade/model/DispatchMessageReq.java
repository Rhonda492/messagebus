/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseRequest;

/**
 * 消息分发请求
 * 
 * @author wangxudong 2016年8月12日 上午11:08:27
 *
 */
public class DispatchMessageReq extends BaseRequest {

    private static final long serialVersionUID = 4598106876219805372L;

    private String appId;

    private String code;

    private String messageId;

    private String messageUuid;

    private String messageBody;

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
     * @return the messageUuid
     */
    public String getMessageUuid() {
        return messageUuid;
    }

    /**
     * @param messageUuid the messageUuid to set
     */
    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    /**
     * @return the messageBody
     */
    public String getMessageBody() {
        return messageBody;
    }

    /**
     * @param messageBody the messageBody to set
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
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
}
