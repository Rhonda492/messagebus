/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

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
     * @param exchange
     * @param queue
     * @param messageBody
     * @param messageId
     * @param messageUuid
     */
    public void invoke(String exchange, String queue, String messageBody, String messageId, String messageUuid);
}
