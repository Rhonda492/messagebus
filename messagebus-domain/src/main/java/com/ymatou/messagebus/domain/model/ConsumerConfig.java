/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * 消费者配置
 * 
 * @author wangxudong 2016年8月5日 下午5:36:06
 *
 */
@Embedded
public class ConsumerConfig {

    /**
     * 是否自动ACK
     */
    @Property("IsAutoAcknowledge")
    private Boolean isAutoAcknowledge;


    /**
     * 最大线程数（用于保护业务方，限流）
     */
    @Property("MaxThreadCount")
    private Integer maxThreadCount;

    /**
     * 预先取的数量
     */
    @Property("PrefetchCount")
    private Integer prefetchCount;

    /**
     * 补单超时
     */
    @Property("RetryTimeOut")
    private Integer retryTimeout;

    /**
     * @return the isAutoAcknowledge
     */
    public Boolean getIsAutoAcknowledge() {
        return isAutoAcknowledge;
    }

    /**
     * @param isAutoAcknowledge the isAutoAcknowledge to set
     */
    public void setIsAutoAcknowledge(Boolean isAutoAcknowledge) {
        this.isAutoAcknowledge = isAutoAcknowledge;
    }

    /**
     * @return the maxThreadCount
     */
    public Integer getMaxThreadCount() {
        return maxThreadCount;
    }

    /**
     * @param maxThreadCount the maxThreadCount to set
     */
    public void setMaxThreadCount(Integer maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    /**
     * @return the prefetchCount
     */
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * @param prefetchCount the prefetchCount to set
     */
    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * @return the retryTimeout
     */
    public Integer getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * @param retryTimeout the retryTimeout to set
     */
    public void setRetryTimeout(Integer retryTimeout) {
        this.retryTimeout = retryTimeout;
    }
}
