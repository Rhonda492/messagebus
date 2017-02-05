/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * 消息配置
 * 
 * @author wangxudong 2016年8月2日 下午5:03:33
 *
 */
@Embedded
public class MessageConfig {

    @Property("Code")
    private String code;

    @Property("Enable")
    private Boolean enable;

    @Property("EnableLog")
    private Boolean enableLog;

    /**
     * Kafka一次抽取的数量
     */
    @Property("PoolSize")
    private Integer poolSize;

    /**
     * 检测进补单时间间隔(分钟)：默认10分钟 ，0 代表关闭检测进补单
     */
    @Property("CheckCompensateDelay")
    private Integer checkCompensateDelay;

    /**
     * 检测进补单时间跨度（小时）：默认48小时 ，0 代表关闭检测进补单
     */
    @Property("CheckCompensateTimeSpan")
    private Integer checkCompensateTimeSpan;

    @Embedded("CallbackCfgList")
    private List<CallbackConfig> callbackCfgList;

    @Embedded("ConsumeCfg")
    private ConsumerConfig consumeCfg;

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the enable
     */
    public Boolean getEnable() {
        if (enable == null) {
            return true;
        } else {
            return enable;
        }
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * @return the callbackCfgList
     */
    public List<CallbackConfig> getCallbackCfgList() {
        if (callbackCfgList == null) {
            callbackCfgList = new ArrayList<CallbackConfig>();
        }

        return callbackCfgList;
    }

    /**
     * @param callbackCfgList the callbackCfgList to set
     */
    public void setCallbackCfgList(List<CallbackConfig> callbackCfgList) {
        this.callbackCfgList = callbackCfgList;
    }

    /**
     * @return the consumeCfg
     */
    public ConsumerConfig getConsumeCfg() {
        return consumeCfg;
    }

    /**
     * @param consumeCfg the consumeCfg to set
     */
    public void setConsumeCfg(ConsumerConfig consumeCfg) {
        this.consumeCfg = consumeCfg;
    }

    /**
     * 根据ConsumerId获取到回调配置
     * 
     * @param consumerId
     * @return
     */
    public CallbackConfig getCallbackConfig(String consumerId) {
        if (callbackCfgList == null) {
            return null;
        }
        Optional<CallbackConfig> findAny = callbackCfgList.stream()
                .filter(callbackCfg -> callbackCfg.getCallbackKey().equals(consumerId)).findAny();

        if (findAny.isPresent()) {
            return findAny.get();
        } else {
            return null;
        }
    }

    /**
     * @return the enableLog
     */
    public Boolean getEnableLog() {
        if (enableLog == null) {
            return true;
        }
        return enableLog;
    }

    /**
     * @param enableLog the enableLog to set
     */
    public void setEnableLog(Boolean enableLog) {
        this.enableLog = enableLog;
    }

    public Integer getPoolSize() {
        if (poolSize == null || poolSize < 1) {
            return 50;
        } else {
            return poolSize;
        }
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Integer getCheckCompensateDelay() {
        if (checkCompensateDelay == null || checkCompensateDelay < 0) {
            return 10; // 不配置代表默认检测10分钟前没有处理的消息, 0 表示关闭检测进入补单
        } else {
            return checkCompensateDelay;
        }
    }

    public void setCheckCompensateDelay(Integer checkCompensateDelay) {
        this.checkCompensateDelay = checkCompensateDelay;
    }

    public Integer getCheckCompensateTimeSpan() {
        if (checkCompensateTimeSpan == null || checkCompensateTimeSpan < 0) {
            return 48; // 不配置代表默认检测48小时内没有处理的消息， 0 表示关闭检测进入补单
        } else {
            return checkCompensateTimeSpan;
        }
    }

    public void setCheckCompensateTimeSpan(Integer checkCompensateTimeSpan) {
        this.checkCompensateTimeSpan = checkCompensateTimeSpan;
    }

    /**
     * 判断是否需要开启检测进入补单模式
     * 
     * @return
     */
    public boolean isNeedCheckCompensate() {
        if (Boolean.TRUE.equals(getEnable()) && Boolean.TRUE.equals(getEnableLog()) && getCheckCompensateDelay() > 0
                && getCheckCompensateTimeSpan() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
