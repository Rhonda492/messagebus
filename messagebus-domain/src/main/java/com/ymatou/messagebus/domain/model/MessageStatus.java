/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;
import com.ymatou.messagebus.infrastructure.net.NetUtil;

/**
 * 消息分发记录
 * 
 * @author wangxudong 2016年8月11日 下午1:51:55
 *
 */
@Entity(noClassnameStored = true)
public class MessageStatus extends PrintFriendliness {

    private static final long serialVersionUID = -5625356542203251142L;

    @Property("_id")
    private String id;

    @Property("uuid")
    private String messageUuid;

    @Property("mid")
    private String messageId;

    @Property("cid")
    private String consumerId;

    @Property("status")
    private String status;

    @Property("source")
    private String source;

    @Property("ctime")
    private Date createTime;

    @Property("result")
    private String result;

    @Property("r_ip")
    private String proccessIp;

    public MessageStatus() {

    }

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
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
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
     * @return the proccessIp
     */
    public String getProccessIp() {
        return proccessIp;
    }

    /**
     * @param proccessIp the proccessIp to set
     */
    public void setProccessIp(String proccessIp) {
        this.proccessIp = proccessIp;
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
     * 添加成功回调结果
     * 
     * @param callbackResult
     */
    public void setSuccessResult(String consumerId, long duration, String callbackUrl) {
        this.result = String.format("ok, %s, %dms, %s", consumerId, duration, callbackUrl);
    }

    /**
     * 添加失败回调结果
     * 
     * @param duration
     * @param exceptionMessage
     * @param response
     * @param callbackUrl
     */
    public void setFailResult(String consumerId, Throwable throwable, long duration, String response,
            String callbackUrl) {
        if (StringUtils.isEmpty(response) || response.equals("fail") || response.equals("\"fail\"")) {
            response = "";
        }

        if (throwable != null) {
            this.result =
                    String.format("fail, %s, %dms, %s:%s, %s", consumerId, duration, throwable.getClass().getName(),
                            throwable.getMessage(), callbackUrl);
        } else {
            this.result = String.format("fail, %s, %dms, %s, %s", consumerId, duration, response, callbackUrl);
        }
    }

    /**
     * @return the consumerId
     */
    public final String getConsumerId() {
        return consumerId;
    }

    /**
     * @param consumerId the consumerId to set
     */
    public final void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    /**
     * @return the result
     */
    public final String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public final void setResult(String result) {
        this.result = result;
    }

    /**
     * 转换成消息转态
     * 
     * @param message
     * @param callbackConfig
     * @return
     */
    public static MessageStatus from(Message message, CallbackConfig callbackConfig) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setConsumerId(callbackConfig.getCallbackKey());
        messageStatus.setCreateTime(new Date());
        messageStatus.setId(UUID.randomUUID().toString());
        messageStatus.setMessageId(message.getMessageId());
        messageStatus.setMessageUuid(message.getUuid());
        messageStatus.setProccessIp(NetUtil.getHostIp());

        return messageStatus;
    }
}
