/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test;

import java.util.Date;
import java.util.UUID;

public class MessageBody {

    private int priority;

    private Date createTime;

    private boolean durable;

    private String bizId;

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
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
     * @return the durable
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * @param durable the durable to set
     */
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * @return the bizId
     */
    public String getBizId() {
        return bizId;
    }

    /**
     * @param bizId the bizId to set
     */
    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public static MessageBody newInstance() {
        MessageBody messageBody = new MessageBody();
        messageBody.setBizId(UUID.randomUUID().toString());
        messageBody.setCreateTime(new Date());
        messageBody.setDurable(true);
        messageBody.setPriority(99);
        return messageBody;
    }
}
