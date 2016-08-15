/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;


/**
 * 消息补单来源
 * 
 * @author wangxudong 2016年8月1日 下午5:23:53
 *
 */
public enum MessageCompensateSourceEnum {

    /**
     * 来自接收站
     */
    Publish(1),


    /**
     * 来自分发站
     */
    Dispatch(2),

    /**
     * 来自补偿站（定时查询 NewStatus == 0, ProcessStatus=0 的消息）
     */
    Compensate(3);

    private Integer code;

    private MessageCompensateSourceEnum(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return this.code;
    }
}
