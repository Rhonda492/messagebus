/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.kafka;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * Kafka消息键
 * 
 * @author wangxudong 2016年10月13日 下午6:30:31
 *
 */
public class KafkaMessageKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1973418533199503811L;

    public KafkaMessageKey() {

    }

    public KafkaMessageKey(String appId, String code, String uuid, String messageId) {
        this.appId = appId;
        this.code = code;
        this.uuid = uuid;
        this.messageId = messageId;
    }

    private String appId;
    private String code;
    private String uuid;
    private String messageId;

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
     * @return the appCode
     */
    public String getAppCode() {
        return String.format("%s_%s", appId, code);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static KafkaMessageKey valueOf(String json) {
        return (KafkaMessageKey) JSON.parseObject(json, KafkaMessageKey.class);
    }
}
