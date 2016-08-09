/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class CallbackInfo {

    @Property("_cid")
    private String callbackKey;

    @Property("_status")
    private Integer status;

    @Property("_count")
    private Integer retryCount;

    @Property("nstatus")
    private Integer newStatus;

    /**
     * @return the callbackKey
     */
    public String getCallbackKey() {
        return callbackKey;
    }

    /**
     * @param callbackKey the callbackKey to set
     */
    public void setCallbackKey(String callbackKey) {
        this.callbackKey = callbackKey;
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
