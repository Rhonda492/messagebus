/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    /**
     * 主键
     */
    @Property("_id")
    private String id;

    /**
     * 状态
     * 
     * @see com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum
     */
    @Property("status")
    private Integer status;

    /**
     * 应用Id
     */
    @Property("appid")
    private String appId;

    /**
     * 业务代码
     */
    @Property("code")
    private String code;

    /**
     * 消息Id
     */
    @Property("mid")
    private String messageId;

    /**
     * 消息体
     */
    @Property("body")
    private String body;

    /**
     * 创建时间
     */
    @Property("ctime")
    private Date createTime;

    /**
     * 重试过期时间
     */
    @Property("rtimeout")
    private Date retryTimeout;

    /**
     * 
     */
    @Property("appkey")
    private String appKey;

    /**
     * 重试次数
     */
    @Property("retrycount")
    private Integer retryCount;

    /**
     * 消费者列表
     */
    @Embedded("callback")
    private List<CallbackInfo> callbackList = new ArrayList<CallbackInfo>();

    /**
     * 补单状态 JAVA版
     */
    @Property("nstatus")
    private Integer newStatus;

    /**
     * 来源：1-接收站，2-分发站，3-补单站
     */
    @Property("source")
    private Integer source;

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
    public String getCode() {
        return code;
    }

    /**
     * @param appCode the appCode to set
     */
    public void setCode(String appCode) {
        this.code = appCode;
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
        compensate.setId(message.getUuid());
        compensate.setStatus(MessageCompensateStatusEnum.RetryOk.code()); // 避免.NET补单
        compensate.setNewStatus(MessageCompensateStatusEnum.NotRetry.code());
        compensate.setAppId(message.getAppId());
        compensate.setCode(message.getCode());
        compensate.setMessageId(message.getMessageId());
        compensate.setBody(message.getBody());
        compensate.setCreateTime(new Date());

        // 计算重试截止时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        compensate.setRetryTimeout(calendar.getTime());

        compensate.setAppKey("*");
        compensate.setRetryCount(0);

        for (CallbackConfig config : appConfig.getMessageConfigByAppCode(message.getCode()).getCallbackCfgList()) {
            CallbackInfo callbackInfo = new CallbackInfo();
            callbackInfo.setCallbackKey(config.getCallbackKey());
            callbackInfo.setStatus(MessageCompensateStatusEnum.RetryOk.code());// 避免.NET补单
            callbackInfo.setNewStatus(MessageCompensateStatusEnum.NotRetry.code());
            compensate.callbackList.add(callbackInfo);
        }

        return compensate;
    }


    /**
     * 构建消息补偿实例
     * 
     * @param appId
     * @param code
     * @param messageId
     * @param body
     * @param consumerId
     * @return
     */
    public static MessageCompensate newInstance(String appId, String code, String uuid, String messageId, String body,
            List<CallbackInfo> listCallbackInfo) {
        MessageCompensate compensate = new MessageCompensate();
        compensate.setId(uuid);
        compensate.setStatus(MessageCompensateStatusEnum.RetryOk.code()); // 避免.NET补单
        compensate.setNewStatus(MessageCompensateStatusEnum.NotRetry.code());
        compensate.setAppId(appId);
        compensate.setCode(code);
        compensate.setMessageId(messageId);
        compensate.setBody(body);
        compensate.setCreateTime(new Date());

        // 计算重试截止时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        compensate.setRetryTimeout(calendar.getTime());

        compensate.setAppKey("*");
        compensate.setRetryCount(0);

        // 添加消费者信息
        compensate.setCallbackList(listCallbackInfo);

        return compensate;
    }

    /**
     * @return the source
     */
    public Integer getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(Integer source) {
        this.source = source;
    }

    /**
     * @return the newStatus
     */
    public Integer getNewStatus() {
        return newStatus;
    }

    /**
     * @param newStatus the newStatus to set
     */
    public void setNewStatus(Integer newStatus) {
        this.newStatus = newStatus;
    }
}
