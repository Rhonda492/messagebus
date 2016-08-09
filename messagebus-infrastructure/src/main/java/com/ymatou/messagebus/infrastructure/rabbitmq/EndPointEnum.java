/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

/**
 * 终结点类型
 * 
 * @author wangxudong 2016年8月9日 下午5:55:31
 *
 */
public enum EndPointEnum {

    /**
     * 生产者
     */
    PRODUCER,

    /**
     * 消费者
     */
    CONSUMER
}
