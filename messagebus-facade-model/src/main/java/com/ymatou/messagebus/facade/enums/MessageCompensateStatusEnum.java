/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;


/**
 * 消息重试状态
 * 
 * @author wangxudong 2016年8月1日 下午5:23:53
 *
 */
public enum MessageCompensateStatusEnum {
    // 未补发（初始化）
    NotRetry(0),

    // 补发中
    Retrying(1),

    // 补发成功
    RetryOk(2),

    // 补发失败
    RetryFail(3);

    private Integer code;

    private MessageCompensateStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
