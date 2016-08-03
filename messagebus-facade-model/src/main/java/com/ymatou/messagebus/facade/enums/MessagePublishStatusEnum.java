/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;


/**
 * 消息推送状态
 * 
 * @author wangxudong 2016年8月1日 下午5:23:53
 *
 */
public enum MessagePublishStatusEnum {
    // 初始化
    Init(0),

    // 已推送 [1000]
    AlreadyPush(1000),

    // 已重试 [1300]
    AlreadyRetry(1300),

    // 直接把消息存储到DB [1400]
    ClientPush(1400),

    // 客户端已消费 [1600]
    ClientAlreadyPush(1600);

    private Integer code;

    private MessagePublishStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
