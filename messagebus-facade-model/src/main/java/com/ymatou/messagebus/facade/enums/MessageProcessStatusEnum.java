/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;

/**
 * @author tony 2016年8月14日 下午8:40:17
 *
 */
public enum MessageProcessStatusEnum {

    // 未处理
    Init(0),

    // 分发成功
    Success(1),

    // 补单中
    Compensate(2),

    // 分发失败
    Fail(3);

    private Integer code;

    private MessageProcessStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
