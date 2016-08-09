/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;


/**
 * 消息状态JAVA版
 * 
 * @author wangxudong 2016年8月1日 下午5:23:53
 *
 */
public enum MessageNewStatusEnum {
    // 进入RabbitMQ
    InRabbitMQ(0),

    // 接收进入补单
    PublishToCompensate(1),

    // 分发进入补单
    DispatchToCompensate(2),

    // 检测进入补单
    CheckToCompensate(3),

    // 分发成功
    Success(90),

    // 接收进入补单 分发成功
    PublishToCompensateSuccess(91),

    // 分发进入补单 分发成功
    DispatchToCompensateSuccess(92),

    // 检测进入补单 分发成功
    CheckToCompensateSuccess(93);

    private Integer code;

    private MessageNewStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
