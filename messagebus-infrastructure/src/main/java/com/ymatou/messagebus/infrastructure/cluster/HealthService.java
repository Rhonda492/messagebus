/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.cluster;

public interface HealthService {

    /**
     * 返回服务的健康状态
     * 
     * @return
     */
    public boolean isHealth();
}
