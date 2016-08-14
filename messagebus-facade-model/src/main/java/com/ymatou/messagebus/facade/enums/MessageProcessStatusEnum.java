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

    /**
     * 根据补单状态计算消息状态
     * 
     * @param compensateStatusEnum
     * @return
     */
    public static MessageProcessStatusEnum from(MessageCompensateStatusEnum compensateStatusEnum) {
        switch (compensateStatusEnum) {
            case NotRetry:
                return MessageProcessStatusEnum.Compensate;
            case RetryFail:
                return MessageProcessStatusEnum.Fail;
            case Retrying:
                return MessageProcessStatusEnum.Compensate;
            case RetryOk:
                return MessageProcessStatusEnum.Success;
            default:
                return MessageProcessStatusEnum.Compensate;
        }
    }
}
