/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;


/**
 * 消息中间件类型
 * 
 * @author wangxudong 2016年8月1日 下午5:23:53
 *
 */
public enum MQTypeEnum {

    RabbitMQ(0),

    Kafka(1);

    private Integer code;

    private MQTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
