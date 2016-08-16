/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.config;

import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

@Component
@DisconfFile(fileName = "rabbitmq.properties")
public class RabbitMQConfig {
    /**
     * 主集群链接串
     */
    private String primaryUri;

    /**
     * 备用集群链接串
     */
    private String secondaryUri;

    /**
     * @return the primaryUri
     */
    @DisconfFileItem(name = "rabbitmq.primary.uri")
    public String getPrimaryUri() {
        return primaryUri;
    }

    /**
     * @param primaryUri the primaryUri to set
     */
    public void setPrimaryUri(String primaryUri) {
        this.primaryUri = primaryUri;
    }

    /**
     * @return the secondaryUri
     */
    @DisconfFileItem(name = "rabbitmq.secondary.uri")
    public String getSecondaryUri() {
        return secondaryUri;
    }

    /**
     * @param secondaryUri the secondaryUri to set
     */
    public void setSecondaryUri(String secondaryUri) {
        this.secondaryUri = secondaryUri;
    }


}
