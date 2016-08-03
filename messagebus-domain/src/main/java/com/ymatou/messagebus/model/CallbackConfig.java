/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * 回调配置
 * 
 * @author wangxudong 2016年8月2日 下午5:01:22
 *
 */
@Embedded
public class CallbackConfig {
    @Property("CallbackKey")
    private String callbackKey;

    @Property("Url")
    private String url;

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
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
