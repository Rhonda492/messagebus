/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
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
    @Property("code")
    private String code;

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
    private Date createTime;

    /*
     * 推送状态 0：未推送 1000：已推送 1300：已重试 1400：直接存储到DB 1600: 客户端已消费
     */
    @Property("pushstatus")
    private Integer pushStatus;

    /**
     * 消息状态（Java版）
     * 
     * 0-进入RabbitMQ 1-接收进入补单 2-分发进入补单 3-检测进入补单
     */
    @Property("nstatus")
    private Integer newStatus;

    /**
     * 处理结果（Java版）
     * 
     * 0-未处理 1-通知成功 2-补单中 3-处理失败
     */
    @Property("pstatus")
    private Integer processStatus;

    /**
     * 推送时间
     */
    @Property("pushtime")
    private Date pushTime;

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
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the pushStatus
     */
    public Integer getPushStatus() {
        return pushStatus;
    }

    /**
     * @param pushStatus the pushStatus to set
     */
    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    /**
     * @return the pushTime
     */
    public Date getPushTime() {
        return pushTime;
    }

    /**
     * @param pushTime the pushTime to set
     */
    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    /**
     * @return the appCode
     */
    public String getAppCode() {
        return String.format("%s_%s", appId, code);
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

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    /**
     * 生成MessageUuid: {yyyyMM}.{ObjectId}
     * 
     * @return
     */
    public static String newUuid() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        return String.format("%s.%s", dateFormat.format(new Date()), ObjectId.get().toString());
    }
}
