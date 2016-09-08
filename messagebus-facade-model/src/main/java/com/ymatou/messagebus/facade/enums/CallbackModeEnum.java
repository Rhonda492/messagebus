/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.enums;

/**
 * 回调模式
 * 
 * @author wangxudong 2016年9月8日 下午5:53:29
 *
 */
public enum CallbackModeEnum {

    /**
     * 分发回调
     */
    Dispatch,

    /**
     * 补单回调
     */
    Compensate,

    /**
     * 秒级补单
     * 部分应用需要在较短时间内完成补单，此时补单的消息来自分发站，不通过查询MongoDB完成
     */
    SecondCompensate
}
