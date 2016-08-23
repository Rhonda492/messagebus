/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

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
    /**
     * 回调配置Id
     */
    @Property("CallbackKey")
    private String callbackKey;

    /**
     * 回调Url
     */
    @Property("Url")
    private String url;

    /**
     * 是否启用
     */
    @Property("Enable")
    private Boolean enable;

    /**
     * 是否重试
     */
    @Property("IsRetry")
    private Integer isRetry;

    /**
     * 业务端接受的HTTP类型，默认POST
     */
    @Property("HttpMethod")
    private String httpMethod;

    /**
     * 业务端接受的媒体类型，默认application/json
     */
    @Property("ContentType")
    private String contentType;

    /**
     * 回调业务端超时时间（0：不超时，大于0 则表示超时），默认不超时
     */
    @Property("CallbackTimeOut")
    private Integer callbackTimeOut;

    /**
     * 并发度，用于保护业务系统
     */
    @Property("ParallelismNum")
    private Integer parallelismNum;

    /**
     * 超时时间，默认10分钟，单位分钟
     */
    @Property("RetryTimeout")
    private Integer retryTimeout;

    /**
     * 订阅者AppId
     */
    @Property("CallbackAppId")
    private String callbackAppId;

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

    /**
     * @return the enable
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * @return the isRetry
     */
    public Integer getIsRetry() {
        return isRetry;
    }

    /**
     * @param isRetry the isRetry to set
     */
    public void setIsRetry(Integer isRetry) {
        this.isRetry = isRetry;
    }

    /**
     * @return the httpMethod
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod the httpMethod to set
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the callbackTimeOut
     */
    public Integer getCallbackTimeOut() {
        return callbackTimeOut;
    }

    /**
     * @param callbackTimeOut the callbackTimeOut to set
     */
    public void setCallbackTimeOut(Integer callbackTimeOut) {
        this.callbackTimeOut = callbackTimeOut;
    }

    /**
     * @return the parallelismNum
     */
    public final Integer getParallelismNum() {
        return parallelismNum;
    }

    /**
     * @param parallelismNum the parallelismNum to set
     */
    public final void setParallelismNum(Integer parallelismNum) {
        this.parallelismNum = parallelismNum;
    }

    /**
     * 获取到超时时间（没填或者<50,返回默认值5000）
     * 
     * @return
     */
    public int getTimeout() {
        Integer timeout = getCallbackTimeOut();
        if (timeout == null || timeout.intValue() < 50) {
            return 5000;
        } else {
            return timeout.intValue();
        }
    }

    /**
     * @return the retryTimeout
     */
    public final Integer getRetryTimeout() {
        if (retryTimeout == null) {
            return 30;
        } else {
            return retryTimeout;
        }
    }

    /**
     * @param retryTimeout the retryTimeout to set
     */
    public final void setRetryTimeout(Integer retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    /**
     * @return the callbackAppId
     */
    public final String getCallbackAppId() {
        return callbackAppId;
    }

    /**
     * @param callbackAppId the callbackAppId to set
     */
    public final void setCallbackAppId(String callbackAppId) {
        this.callbackAppId = callbackAppId;
    }
}
