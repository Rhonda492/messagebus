/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;

/**
 * 消息补偿记录
 * 
 * @author wangxudong 2016年8月2日 下午6:15:39
 *
 */
@Entity(noClassnameStored = true)
public class MessageCompensate extends PrintFriendliness {

    private static final long serialVersionUID = 7869866217863020411L;

    @Property("_id")
    private String id;

    @Property("status")
    private Integer status;

    @Property("appid")
    private String appId;

    @Property("code")
    private String appCode;

    @Property("mid")
    private String messageId;

    @Property("body")
    private String body;

    @Property("ctime")
    private Date createTime;

    @Property("rtimeout")
    private Date retryTimeout;

    @Property("appkey")
    private String appKey;

    @Property("retrycount")
    private Integer retryCount;

    @Embedded("callback")
    private List<CallbackInfo> callbackList = new ArrayList<CallbackInfo>();

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

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
     * @return the appCode
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * @param appCode the appCode to set
     */
    public void setAppCode(String appCode) {
        this.appCode = appCode;
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
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the retryTimeout
     */
    public Date getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * @param retryTimeout the retryTimeout to set
     */
    public void setRetryTimeout(Date retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    /**
     * @return the appKey
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * @param appKey the appKey to set
     */
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    /**
     * @return the retryCount
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * @param retryCount the retryCount to set
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * @return the callbackList
     */
    public List<CallbackInfo> getCallbackList() {
        return callbackList;
    }

    /**
     * @param callbackList the callbackList to set
     */
    public void setCallbackList(List<CallbackInfo> callbackList) {
        this.callbackList = callbackList;
    }

    /**
     * 从消息到补偿的模型装换
     * 
     * @param appConfig
     * @param message
     * @return
     */
    public static MessageCompensate from(AppConfig appConfig, Message message) {
        MessageCompensate compensate = new MessageCompensate();
        compensate.setId(UUID.randomUUID().toString());
        compensate.setStatus(MessageCompensateStatusEnum.NotRetry.code());
        compensate.setAppId(message.getAppId());
        compensate.setAppCode(message.getAppCode());
        compensate.setMessageId(message.getMessageId());
        compensate.setBody(message.getBody());
        compensate.setCreateTime(new Date());

        // 计算重试截止时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        compensate.setRetryTimeout(calendar.getTime());

        compensate.setAppKey("*");
        compensate.setRetryCount(0);

        for (CallbackConfig config : appConfig.getMessageConfig(message.getCode()).getCallbackCfgList()) {
            CallbackInfo callbackInfo = new CallbackInfo();
            callbackInfo.setCallbackKey(config.getCallbackKey());
            callbackInfo.setStatus(MessageCompensateStatusEnum.NotRetry.code());
            compensate.callbackList.add(callbackInfo);
        }

        return compensate;
    }
}
