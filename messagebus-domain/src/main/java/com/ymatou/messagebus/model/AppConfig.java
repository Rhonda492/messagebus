/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.model;


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

    @Property("_id")
    public String appId;

    @Property("Version")
    public Integer version;

    @Property("ConnCfg")
    public Object connCfg;

    @Embedded("MessageCfgList")
    public List<MessageConfig> messageCfgList;

    @Property("OwnerHost")
    public String ownerHost;


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
     * @return the connCfg
     */
    public Object getConnCfg() {
        return connCfg;
    }

    /**
     * @param connCfg the connCfg to set
     */
    public void setConnCfg(Object connCfg) {
        this.connCfg = connCfg;
    }

    /**
     * @return the messageCfgList
     */
    public List<MessageConfig> getMessageCfgList() {
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
     * 根据code查找消息配置
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
}
