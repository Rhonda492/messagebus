/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

public interface DispatchKafkaResource {

    /**
     * 启动分发服务
     * 
     * @return
     */
    public String start();


    /**
     * 暂停分发服务
     * 
     * @return
     */
    public String stop();
}
