/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseRequest;

/**
 * 检测出需要补偿的消息 Req
 * 
 * @author tony 2016年8月14日 下午4:03:58
 *
 */
public class CheckToCompensateReq extends BaseRequest {

    private static final long serialVersionUID = -5129679946217628080L;

    private String appId;

    private String code;

    /**
     * @return the appId
     */
    public final String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public final void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the code
     */
    public final String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public final void setCode(String code) {
        this.code = code;
    }

}
