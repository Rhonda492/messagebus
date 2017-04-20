/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.config;

import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author luoshiqian 2017/4/20 17:30
 */
@Component
@DisconfUpdateService(confFileKeys = "forward.properties")
public class ForwardConfigUpdateService implements IDisconfUpdate {

    @Autowired
    private ForwardConfig forwardConfig;

    @Override
    public void reload() throws Exception {
        forwardConfig.reload();
    }
}
