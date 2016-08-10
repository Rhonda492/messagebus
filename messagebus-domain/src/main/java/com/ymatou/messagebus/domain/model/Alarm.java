/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * 报警配置
 * 
 * @author wangxudong 2016年8月10日 下午2:01:42
 *
 */
@Entity(value = "Alarm", noClassnameStored = true)
public class Alarm {

    /**
     * Id
     */
    @Property("_id")
    private String id;

    /**
     * 回调地址
     */
    @Property("CallbackUrl")
    private String callbackUrl;

    /**
     * 报警AppId
     */
    @Property("AlarmAppId")
    private String alarmAppId;

    /**
     * 描述
     */
    @Property("Description")
    private String description;

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
     * @return the callbackUrl
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * @param callbackUrl the callbackUrl to set
     */
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * @return the alarmAppId
     */
    public String getAlarmAppId() {
        return alarmAppId;
    }

    /**
     * @param alarmAppId the alarmAppId to set
     */
    public void setAlarmAppId(String alarmAppId) {
        this.alarmAppId = alarmAppId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
