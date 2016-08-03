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

    RABBITMQ_PRODUCER_INIT_FAILED(3000, "队列消费者初始化异常"),

    RABBITMQ_PRODUCER_PUBLISH_FAILED(3001, "队列消费者发布消息异常"),

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
