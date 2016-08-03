/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.model;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.NotSaved;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;

/**
 * 总线消息实体
 * 
 * @author wangxudong 2016年8月1日 下午1:52:15
 *
 */
@Entity(noClassnameStored = true)
public class Message extends PrintFriendliness {
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    @Property("_id")
    private String id;

    /**
     * 唯一标识
     */
    @Property("uuid")
    private String uuid;

    /**
     * 应用Id
     */
    @Property("aid")
    private String appId;

    /**
     * 业务code
     */
    @NotSaved
    private String code;

    /**
     * 业务AppCode: app_code
     */
    @Property("code")
    private String appCode;

    /**
     * 消息Id
     */
    @Property("mid")
    private String messageId;

    /**
     * Ip
     */
    @Property("ip")
    private String ip;

    /**
     * 总线接收服务器Ip
     */
    @Property("busIp")
    private String busReceivedServerIp;

    /**
     * 消息体
     */
    @Property("body")
    private String body;

    /**
     * 创建时间
     */
    @Property("ctime")
    private Date CreateTime;

    /*
     * 推送状态
     * 0：未推送
     * 1000：已推送
     * 1300：已重试
     * 1400：直接存储到DB
     * 1600: 客户端已消费
     */
    @Property("pushstatus")
    private Integer PushStatus;

    /**
     * 推送时间
     */
    @Property("pushtime")
    private Date PushTime;

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
     * @return the busReceivedServerIp
     */
    public String getBusReceivedServerIp() {
        return busReceivedServerIp;
    }

    /**
     * @param busReceivedServerIp the busReceivedServerIp to set
     */
    public void setBusReceivedServerIp(String busReceivedServerIp) {
        this.busReceivedServerIp = busReceivedServerIp;
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
        return CreateTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        CreateTime = createTime;
    }

    /**
     * @return the pushStatus
     */
    public Integer getPushStatus() {
        return PushStatus;
    }

    /**
     * @param pushStatus the pushStatus to set
     */
    public void setPushStatus(Integer pushStatus) {
        PushStatus = pushStatus;
    }

    /**
     * @return the pushTime
     */
    public Date getPushTime() {
        return PushTime;
    }

    /**
     * @param pushTime the pushTime to set
     */
    public void setPushTime(Date pushTime) {
        PushTime = pushTime;
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
}
