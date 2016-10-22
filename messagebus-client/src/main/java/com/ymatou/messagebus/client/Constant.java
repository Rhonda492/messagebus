/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.client;

/**
 * 常量
 * 
 * @author wangxudong 2016年10月14日 下午3:41:52
 *
 */
public class Constant {

    /**
     * 版本号
     * 1.0.3-优化获取本机IP的性能
     * 1.0.4-修复MapDB关闭BUG
     * 1.0.5-修改sendMessage方法名，修复补单BUG
     * 1.0.6-修复MessageLocalConsumer中对于Facade放回的处理
     * 1.0.8-增加Component注解
     * 1.0.9-取消Component注解
     * 1.0.10-增加notWriteDefaultValue字段控制消息的JSON序列化
     */
    public static final String VERSION = "1.0.10";

    /**
     * RabbitMQ的MapName
     */
    public static final String RABBITMQ_MAPNAME = "message";


    /**
     * Kafka的MapName
     */
    public static final String KAFKA_MAPNAME = "message_kafka";
}
