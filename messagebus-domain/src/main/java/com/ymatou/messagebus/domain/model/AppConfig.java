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
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.ymatou.messagebus.facade.PrintFriendliness;


/**
 * App应用配置
 * 
 * @author wangxudong 2016年7月29日 下午5:33:10
 *
 */
@Entity(value = "MQ_App_Cfg", noClassnameStored = true)
public class AppConfig extends PrintFriendliness {

    private static final long serialVersionUID = -7585171825610208707L;

    /**
     * 应用Id
     */
    @Property("_id")
    private String appId;

    /**
     * 配置版本
     */
    @Property("Version")
    private Integer version;

    /**
     * 消息配置列表
     */
    @Embedded("MessageCfgList")
    private List<MessageConfig> messageCfgList;

    /**
     * 处理主机
     */
    @Property("OwnerHost")
    private String ownerHost;

    /**
     * 分发组
     */
    @Property("DispatchGroup")
    private String dispatchGroup;

    /*
     * 消息中间件类型
     */
    @Property("MQType")
    private Integer mqType;


    /**
     * @return the version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }


    /**
     * @return the messageCfgList
     */
    public List<MessageConfig> getMessageCfgList() {
        if (messageCfgList == null) {
            messageCfgList = new ArrayList<MessageConfig>();
        }

        return messageCfgList;
    }

    /**
     * @param messageCfgList the messageCfgList to set
     */
    public void setMessageCfgList(List<MessageConfig> messageCfgList) {
        this.messageCfgList = messageCfgList;
    }

    /**
     * @return the ownerHost
     */
    public String getOwnerHost() {
        return ownerHost;
    }

    /**
     * @param ownerHost the ownerHost to set
     */
    public void setOwnerHost(String ownerHost) {
        this.ownerHost = ownerHost;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the dispatchGroup
     */
    public String getDispatchGroup() {
        return dispatchGroup;
    }

    /**
     * @param dispatchGroup the dispatchGroup to set
     */
    public void setDispatchGroup(String dispatchGroup) {
        this.dispatchGroup = dispatchGroup;
    }

    /**
     * 拼装AppCode
     * 
     * @param code
     * @return
     */
    public String getAppCode(String code) {
        return String.format("%s_%s", this.appId, code);
    }

    /**
     * 根据Code查找消息配置
     * 
     * @param code
     * @return
     */
    public MessageConfig getMessageConfig(String code) {
        if (messageCfgList == null || messageCfgList.isEmpty())
            return null;

        Optional<MessageConfig> findFirst =
                messageCfgList.stream().filter(cfg -> cfg.getCode().equals(code)).findFirst();
        if (!findFirst.isPresent())
            return null;

        return findFirst.get();
    }

    /**
     * 根据AppCode查找消息配置
     * 
     * @param appCode
     * @return
     */
    public MessageConfig getMessageConfigByAppCode(String appCode) {
        String code = appCode.substring(getAppId().length() + 1);
        return getMessageConfig(code);
    }

    /**
     * @return the mqType
     */
    public Integer getMqType() {
        return mqType;
    }

    /**
     * @param mqType the mqType to set
     */
    public void setMqType(Integer mqType) {
        this.mqType = mqType;
    }
}
