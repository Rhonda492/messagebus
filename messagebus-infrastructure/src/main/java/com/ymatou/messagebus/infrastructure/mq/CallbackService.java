/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.mq;

/**
 * 分发回调服务
 * 
 * @author wangxudong 2016年8月5日 下午4:51:20
 *
 */
public interface CallbackService {

    /**
     * 回调服务
     * 
     * @param appId
     * @param appCode
     * @param messageBody
     * @param messageId
     * @param messageUuid
     */
    public void invoke(String appId, String appCode, String messageBody, String messageId, String messageUuid);
}
