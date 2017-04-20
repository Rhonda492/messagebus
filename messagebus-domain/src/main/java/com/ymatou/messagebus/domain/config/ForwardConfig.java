/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * @author luoshiqian 2017/4/20 14:54
 */
@Component
@DisconfFile(fileName = "forward.properties")
@DisconfUpdateService
public class ForwardConfig  {


    private boolean forward;

    private boolean forwardAll;

    private String forwardQueueCode;

    private Set<String> queueCodeSet = Sets.newHashSet();

    @DisconfFileItem(name = "forward")
    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    @DisconfFileItem(name = "forwardAll")
    public boolean isForwardAll() {
        return forwardAll;
    }

    public void setForwardAll(boolean forwardAll) {
        this.forwardAll = forwardAll;
    }

    @DisconfFileItem(name = "forwardQueueCode")
    public String getForwardQueueCode() {
        return forwardQueueCode;
    }

    public void setForwardQueueCode(String forwardQueueCode) {
        this.forwardQueueCode = forwardQueueCode;
    }

    public boolean isNeedForward(String queueCode) {
        if(StringUtils.isNotBlank(forwardQueueCode) && queueCodeSet.isEmpty()){
            synchronized (this){
                if(queueCodeSet.isEmpty()){
                    for(String str: forwardQueueCode.split("[,]")){
                        queueCodeSet.add(str.trim());
                    }
                }
            }
        }
        if (forward && (forwardAll || queueCodeSet.contains(queueCode))) {
            return true;
        }
        return false;
    }

    public void reload(){
        queueCodeSet.clear();
        for(String str: forwardQueueCode.split("[,]")){
            queueCodeSet.add(str.trim());
        }
    }
}
