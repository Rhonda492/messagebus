/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;
import com.ymatou.messagebus.facade.enums.MessageStatusEnum;
import com.ymatou.messagebus.facade.enums.MessageStatusSourceEnum;
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

    @Property("_mid")
    private String messageId;

    @Property("status")
    private String status;

    @Property("source")
    private String source;

    @Property("ctime")
    private Date createTime;

    @Property("_cid")
    private List<String> callbackResult = new ArrayList<String>();

    @Property("r_ip")
    private String proccessIp;

    public MessageStatus() {

    }

    public MessageStatus(String uuid, String messageId, MessageStatusEnum status, MessageStatusSourceEnum source) {
        this.id = uuid;
        this.messageUuid = uuid;
        this.messageId = messageId;
        this.status = status.toString();
        this.source = source.toString();
        this.createTime = new Date();
        this.proccessIp = NetUtil.getHostIp();
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
     * @return the callbackResult
     */
    public List<String> getCallbackResult() {
        return callbackResult;
    }

    /**
     * @param callbackResult the callbackResult to set
     */
    public void setCallbackResult(List<String> callbackResult) {
        this.callbackResult = callbackResult;
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
    public void addSuccessResult(String consumerId, long duration, String callbackUrl) {
        this.callbackResult.add(String.format("ok, %s, %dms, %s", consumerId, duration, callbackUrl));
    }

    /**
     * 添加失败回调结果
     * 
     * @param duration
     * @param exceptionMessage
     * @param response
     * @param callbackUrl
     */
    public void addFailResult(String consumerId, String exceptionMessage, long duration, String response,
            String callbackUrl) {
        if (StringUtils.isEmpty(response) || response.equals("fail") || response.equals("\"fail\"")) {
            response = "";
        }

        if (StringUtils.isEmpty(exceptionMessage)) {
            this.callbackResult
                    .add(String.format("fail, %s, %dms, %s, %s", consumerId, duration, exceptionMessage,
                            callbackUrl));
        } else {
            this.callbackResult
                    .add(String.format("fail, %s, %dms, %s, %s", consumerId, duration, response, callbackUrl));
        }
    }
}
