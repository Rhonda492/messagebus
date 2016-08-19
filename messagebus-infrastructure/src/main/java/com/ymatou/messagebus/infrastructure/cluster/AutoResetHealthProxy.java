/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.cluster;

/**
 * 自动重置健康代理
 * 
 * @author wangxudong 2016年8月19日 下午2:31:25
 *
 */
public class AutoResetHealthProxy implements HealthService {

    /**
     * 健康状态
     */
    private boolean health;

    /**
     * 自动重置的毫秒数
     */
    private int autoResetMSecond;

    /**
     * 最后损坏的时间
     */
    private long lastBrokenTimeMillis;

    /**
     * 构造函数
     * 
     * @param autoResetMSecond
     */
    public AutoResetHealthProxy(int autoResetMSecond) {
        this.health = true;
        this.autoResetMSecond = autoResetMSecond;

        if (autoResetMSecond <= 0) {
            throw new IllegalArgumentException("autoResetMSecond mast big then zero.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.cluster.HealthService#isHealth()
     */
    @Override
    public boolean isHealth() {
        if (health) {
            return true;
        }

        if (System.currentTimeMillis() - lastBrokenTimeMillis > autoResetMSecond) {
            health = true;
        }

        return health;
    }

    /**
     * 设置健康代理损坏
     */
    public void setBroken() {
        this.health = false;
        this.lastBrokenTimeMillis = System.currentTimeMillis();
    }

    /**
     * 设置自动重置毫秒数
     * 
     * @param autoResetMSecond
     */
    public void setAutoResetMSecond(int autoResetMSecond) {
        this.autoResetMSecond = autoResetMSecond;
    }
}
