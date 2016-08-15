/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseRequest;

/**
 * 补单请求
 * 
 * @author wangxudong 2016年8月15日 上午11:41:26
 *
 */
public class CompensateReq extends BaseRequest {

    private static final long serialVersionUID = -4858979537944209622L;

    /**
     * 应用Id
     */
    private String appId;

    /**
     * 业务代码
     */
    private String code;

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

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

}
