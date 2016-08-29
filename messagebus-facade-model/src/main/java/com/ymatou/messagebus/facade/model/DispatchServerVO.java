/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

public class DispatchServerVO {


    /**
     * 分发组
     */
    private String groupId;

    /**
     * 本机Ip
     */
    private String ip;

    /**
     * 主机名
     */
    private String hostName;

    /**
     * 消费列表
     */
    private Object consumerMap;


    /**
     * 终结点列表
     */
    private Object endPointMap;

    /**
     * 信号量列表
     */
    private Object semaphoreMap;

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the consumerMap
     */
    public Object getConsumerMap() {
        return consumerMap;
    }

    /**
     * @param consumerMap the consumerMap to set
     */
    public void setConsumerMap(Object consumerMap) {
        this.consumerMap = consumerMap;
    }

    /**
     * @return the endPointMap
     */
    public Object getEndPointMap() {
        return endPointMap;
    }

    /**
     * @param endPointMap the endPointMap to set
     */
    public void setEndPointMap(Object endPointMap) {
        this.endPointMap = endPointMap;
    }

    /**
     * @return the semaphoreMap
     */
    public Object getSemaphoreMap() {
        return semaphoreMap;
    }

    /**
     * @param semaphoreMap the semaphoreMap to set
     */
    public void setSemaphoreMap(Object semaphoreMap) {
        this.semaphoreMap = semaphoreMap;
    }
}
