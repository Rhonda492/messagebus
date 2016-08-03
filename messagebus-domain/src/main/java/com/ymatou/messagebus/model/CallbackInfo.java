/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class CallbackInfo {

    @Property("_cid")
    public String callbackKey;

    @Property("_status")
    public Integer status;

    @Property("_count")
    public Integer retryCount;

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
}
