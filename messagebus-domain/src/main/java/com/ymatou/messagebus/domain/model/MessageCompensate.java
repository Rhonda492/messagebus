/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageCompensateStatusEnum;

/**
 * 消息补偿记录
 * 
 * @author wangxudong 2016年8月2日 下午6:15:39
 *
 */
/**
 * @author wangxudong 2016年9月13日 上午10:31:13
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
     * Message UUID
     */
    @Property("uuid")
    private String messageUuid;

    /**
     * 消费者Id
     */
    @Property("cid")
    private String consumerId;

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
     * 重试时间(下一次补单时间)
     */
    @Property("rtime")
    private Date retryTime;

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
     * 重试次数（包含秒补）
     */
    @Property("retrycount")
    private Integer retryCount;

    /**
     * 补偿次数（不含秒补）
     */
    @Property("compensatecount")
    private Integer compensateCount;

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
     * 判断是否需要重试
     * 
     * @param retryProlicy
     * @return
     */
    public boolean needRetry(String retryProlicy) {
        if (new Date().after(retryTimeout)) { // 超过了重试时间
            return false;
        }

        int index = 0;
        if (compensateCount != null) {
            index = compensateCount.intValue();
        }

        if (!StringUtils.isEmpty(retryProlicy) // 填写了重试策略
                && !retryProlicy.contains("e") // 重试策略不是无限终止
                && retryProlicy.split("-").length <= index) { // 重试策略已经用完
            return false;
        }

        return true;
    }

    /**
     * 从消息到补偿的模型装换
     * 
     * @param appConfig
     * @param message
     * @return
     */
    public static MessageCompensate from(Message message, CallbackConfig callbackConfig,
            MessageCompensateSourceEnum sourceEnum) {
        MessageCompensate compensate = new MessageCompensate();
        compensate.setId(UUID.randomUUID().toString());
        compensate.setMessageUuid(message.getUuid());
        compensate.setStatus(MessageCompensateStatusEnum.RetryOk.code()); // 避免.NET补单
        compensate.setNewStatus(MessageCompensateStatusEnum.NotRetry.code());
        compensate.setAppId(message.getAppId());
        compensate.setCode(message.getCode());
        compensate.setMessageId(message.getMessageId());
        compensate.setBody(message.getBody());
        compensate.setCreateTime(new Date());
        compensate.setConsumerId(callbackConfig.getCallbackKey());
        compensate.setRetryCount(0);
        compensate.setCompensateCount(0);
        compensate.setRetryTime(new Date());
        compensate.setSource(sourceEnum.code());

        // 计算重试截止时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, callbackConfig.getRetryTimeout());
        compensate.setRetryTimeout(calendar.getTime());

        // 计算下次重试时间
        compensate.setRetryTime(calcRetryTime(null, 0, callbackConfig.getRetryPolicy()));

        return compensate;
    }

    /**
     * 设置重试时间
     * 
     * @param retryProlicy
     */
    public void setRetryTime(String retryProlicy) {
        setRetryTime(calcRetryTime(null, compensateCount, retryProlicy));
    }

    /**
     * 计算出下次重试时间
     * 
     * @param lstRetryTime
     * @param index
     * @param retryProlicy
     * @return
     */
    public static Date calcRetryTime(Date lstRetryTime, int index, String retryProlicy) {
        Calendar calendar = Calendar.getInstance();
        if (lstRetryTime != null) {
            calendar.setTime(lstRetryTime);
        }

        int nextMinute = 1;
        if (!StringUtils.isEmpty(retryProlicy)) {
            nextMinute = calcNextRetryMinute(index, retryProlicy);
        }
        calendar.add(Calendar.MINUTE, nextMinute);

        return calendar.getTime();
    }

    /**
     * 重试策略（eNh）正则表达式
     */
    private static final Pattern retryPolicyPattern = Pattern.compile("^e(\\d+)h$");

    /**
     * 根据重试策略计算出下一次重试时间距离当前的分钟数
     * 
     * @param index
     * @param retryPolicy
     * @return
     */
    public static int calcNextRetryMinute(int index, String retryPolicy) {
        String[] retryArray = retryPolicy.split("-");
        if (index >= retryArray.length) {
            index = retryArray.length - 1;
        }

        String retryItem = retryArray[index];
        if (retryItem.endsWith("m")) {
            return Integer.parseInt(retryItem.substring(0, retryItem.length() - 1));
        } else if (retryItem.equals("eh")) {
            return 60;
        } else if (retryPolicyPattern.matcher(retryItem).matches()) {
            return Integer.parseInt(retryItem.substring(1, retryItem.length() - 1)) * 60;
        } else if (retryItem.endsWith("h")) {
            return Integer.parseInt(retryItem.substring(0, retryItem.length() - 1)) * 60;
        } else if (retryItem.equals("ed")) {
            return 60 * 24;
        } else if (retryItem.endsWith("d")) {
            return Integer.parseInt(retryItem.substring(0, retryItem.length() - 1)) * 60 * 24;
        } else {
            return 1;
        }
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


    /**
     * 判断是否已经过了失效时间
     * 
     * @return
     */
    public boolean isRetryTimeout() {
        return this.retryTimeout.before(new Date());
    }

    /**
     * 增加重试计数
     */
    public void incRetryCount() {
        if (this.retryCount == null) {
            this.retryCount = 1;
        } else {
            this.retryCount = this.retryCount.intValue() + 1;
        }
    }

    /**
     * 增加补单次数
     */
    public void incCompensateCount() {
        if (this.compensateCount == null) {
            this.compensateCount = 1;
        } else {
            this.compensateCount = this.compensateCount.intValue() + 1;
        }
    }

    /**
     * @return the messageUuid
     */
    public final String getMessageUuid() {
        return messageUuid;
    }

    /**
     * @param messageUuid the messageUuid to set
     */
    public final void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
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
     * @return the retryTime
     */
    public Date getRetryTime() {
        return retryTime;
    }

    /**
     * @param retryTime the retryTime to set
     */
    public void setRetryTime(Date retryTime) {
        this.retryTime = retryTime;
    }

    /**
     * @return the compensateCount
     */
    public Integer getCompensateCount() {
        return compensateCount;
    }

    /**
     * @param compensateCount the compensateCount to set
     */
    public void setCompensateCount(Integer compensateCount) {
        this.compensateCount = compensateCount;
    }
}
