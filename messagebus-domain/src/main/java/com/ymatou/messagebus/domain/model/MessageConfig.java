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
     * 检测进补单时间 间隔
     */
    @Property("CheckCompensateDelay")
    private Integer checkCompensateDelay;

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
        return checkCompensateDelay;
    }

    public void setCheckCompensateDelay(Integer checkCompensateDelay) {
        this.checkCompensateDelay = checkCompensateDelay;
    }

    public Integer getCheckCompensateTimeSpan() {
        return checkCompensateTimeSpan;
    }

    public void setCheckCompensateTimeSpan(Integer checkCompensateTimeSpan) {
        this.checkCompensateTimeSpan = checkCompensateTimeSpan;
    }

    /**
     * 检测进补单时间跨度
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
        return enable;
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
}
