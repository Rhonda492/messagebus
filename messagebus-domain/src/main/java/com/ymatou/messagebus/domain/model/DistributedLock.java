/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.ymatou.messagebus.facade.PrintFriendliness;

/**
 * 消息补偿锁谁获得锁就可以
 * 
 * @author wangxudong 2016年8月12日 下午6:28:54
 *
 */
@Entity(value = "MQ_Distributed_Lock", noClassnameStored = true)
@Indexes({
        @Index(fields = @Field("lockType"), options = @IndexOptions(unique = true))
})
public class DistributedLock extends PrintFriendliness {

    private static final long serialVersionUID = 3362534184463964848L;

    @Id
    private ObjectId id;

    /**
     * 锁类型
     */
    @Property
    private String lockType;

    /**
     * Ip
     */
    private String ip;

    /**
     * 主机名
     */
    private String hostName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 失效时间
     */
    private Date deadTime;

    /**
     * @return the lockType
     */
    public String getLockType() {
        return lockType;
    }

    /**
     * @param lockType the lockType to set
     */
    public void setLockType(String lockType) {
        this.lockType = lockType;
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
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the deadTime
     */
    public Date getDeadTime() {
        return deadTime;
    }

    /**
     * @param deadTime the deadTime to set
     */
    public void setDeadTime(Date deadTime) {
        this.deadTime = deadTime;
    }
}
