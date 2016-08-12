package com.ymatou.messagebus.facade;

/**
 * 
 * @author tuwenjie
 *
 */
public enum ErrorCode {
    /*
     * 参数错误 1000
     */
    ILLEGAL_ARGUMENT(1000, "参数异常"),


    /*
     * 业务逻辑错误 3000
     */

    MESSAGE_PUBLISH_FAIL(3000, "消息发布失败"),


    /**
     * 不存在有效的回调信息
     */
    NOT_EXIST_INVALID_CALLBACK(3001, "不存在有效的回调信息"),

    /*
     * 通用错误 5000
     */
    FAIL(5000, "请求处理失败"),

    /*
     * 系统错误 9999
     */
    UNKNOWN(9999, "未知错误，系统异常");

    private int code;

    private String message;

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 通过代码获取枚举项
     * 
     * @param code
     * @return
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
