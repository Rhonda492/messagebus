/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.cluster;

/**
 * 主备健康监测代理
 * 
 * @author wangxudong 2016年8月4日 下午2:07:27
 *
 */
public class PSHealthProxy implements HealthService {

    /**
     * 主服务
     */
    private HealthService primaryService;

    /**
     * 备服务
     */
    private HealthService secondaryService;

    /**
     * 健康代理
     * 
     * @param primary
     * @param secondary
     */
    public PSHealthProxy(HealthService primary, HealthService secondary) {
        this.primaryService = primary;
        this.secondaryService = secondary;
    }

    @Override
    public boolean isHealth() {
        return primaryService.isHealth() || secondaryService.isHealth();
    }

    /**
     * 监测主服务是否健康
     * 
     * @return
     */
    public boolean primaryHealth() {
        return primaryService.isHealth();
    }


    /**
     * 监测备服务是否健康
     * 
     * @return
     */
    public Boolean secondaryHealth() {
        return secondaryService.isHealth();
    }
}
