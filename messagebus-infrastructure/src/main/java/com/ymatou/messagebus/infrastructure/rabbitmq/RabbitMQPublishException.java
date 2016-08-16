/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

/**
 * RabbitMQ 发布消息异常，此异常表明主备的消息队列都已经失效了
 * 
 * @author wangxudong 2016年7月29日 下午3:06:32
 *
 */
public class RabbitMQPublishException extends Exception {

    private static final long serialVersionUID = -1449767207737296546L;

    public RabbitMQPublishException() {
        super();
    }

    public RabbitMQPublishException(String msg) {
        super(msg);
    }

    public RabbitMQPublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public RabbitMQPublishException(Throwable cause) {
        super(cause);
    }
}

