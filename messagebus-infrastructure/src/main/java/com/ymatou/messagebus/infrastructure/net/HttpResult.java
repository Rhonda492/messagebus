/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.infrastructure.net;

/**
 * Http返回
 * 
 * @author wangxudong 2016年8月5日 下午6:28:15
 *
 */
public class HttpResult {

    /**
     * 状态码
     */
    private int statusCode;

    /**
     * HttpBody
     */
    private String body;

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpResult [statusCode=" + statusCode + ", body=" + body + "]";
    }
}
