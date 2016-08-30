/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

/**
 * 总线发布异常
 * 
 * @author wangxudong 2016年8月30日 下午6:55:13
 *
 */
public class MessageBusException extends Exception {

    private static final long serialVersionUID = -6860662914175577892L;

    public MessageBusException(String message) {
        super(message);
    }

    public MessageBusException(String message, Throwable cause) {
        super(message, cause);
    }
}
