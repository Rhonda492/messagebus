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
@DisconfFile(fileName = "tomcat.properties")
public class TomcatConfig {

    private int port;
    private int connectionTimeout;
    private int maxConnections;
    private int maxThreads;
    private int acceptCount;
    private String uriEncoding;
    private String protocol;

    /**
     * @return the port
     */
    @DisconfFileItem(name = "tomcat.port")
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the connectionTimeout
     */
    @DisconfFileItem(name = "tomcat.connectionTimeout")
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout the connectionTimeout to set
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @return the maxConnections
     */
    @DisconfFileItem(name = "tomcat.maxConnections")
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections the maxConnections to set
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return the maxThreads
     */
    @DisconfFileItem(name = "tomcat.maxThreads")
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * @param maxThreads the maxThreads to set
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * @return the acceptCount
     */
    @DisconfFileItem(name = "tomcat.acceptCount")
    public int getAcceptCount() {
        return acceptCount;
    }

    /**
     * @param acceptCount the acceptCount to set
     */
    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    /**
     * @return the uriEncoding
     */
    @DisconfFileItem(name = "tomcat.uriEncoding")
    public String getUriEncoding() {
        return uriEncoding;
    }

    /**
     * @param uriEncoding the uriEncoding to set
     */
    public void setUriEncoding(String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

    /**
     * @return the protocol
     */
    @DisconfFileItem(name = "tomcat.protocol")
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


}
