/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

import com.ymatou.messagebus.facade.BaseResponse;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.messagebus.facade.PrintFriendliness;

/**
 * 与.NET http客户端适配的响应
 * 
 * @author tuwenjie 2016年5月17日 下午3:08:20
 *
 */
public class RestResp extends PrintFriendliness {

    private static final long serialVersionUID = 256845177242093093L;

    // http响应码
    private int code;

    // 业务错误码描述
    private String message;


    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }



    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }



    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }



    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 根据BaseResponse生成RestResp实例
     * 
     * @param baseResp
     * @return
     */
    public static RestResp newInstance(BaseResponse baseResp) {
        assert baseResp != null;
        RestResp restResp = new RestResp();
        restResp.setMessage(baseResp.getErrorMessage());

        if (baseResp.isSuccess()) {
            restResp.setCode(200); // 成功
        } else {
            ErrorCode errorCode = baseResp.getErrorCode() == null ? ErrorCode.UNKNOWN : baseResp.getErrorCode();
            switch (errorCode) {
                case ILLEGAL_ARGUMENT:
                    restResp.setCode(400); // 参数错误
                    break;
                case UNKNOWN:
                    restResp.setCode(500); // 未知异常
                    break;
                default:
                    restResp.setCode(201); // 业务因明确原因处理失败
            }
        }
        return restResp;
    }
}
