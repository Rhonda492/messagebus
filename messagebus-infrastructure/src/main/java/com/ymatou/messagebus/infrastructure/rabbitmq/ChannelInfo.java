/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.rabbitmq;

import com.rabbitmq.client.Channel;

/**
 * 
 * @author wangxudong 2016年8月18日 下午7:32:02
 *
 */
public class ChannelInfo {

    private Channel channel;

    private String uri;

    public ChannelInfo(Channel channel, String uri) {
        super();
        this.channel = channel;
        this.uri = uri;
    }

    /**
     * @return the channel
     */
    protected final Channel getChannel() {
        return channel;
    }

    /**
     * @param channel the channel to set
     */
    protected final void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * @return the uri
     */
    protected final String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    protected final void setUri(String uri) {
        this.uri = uri;
    }
}
