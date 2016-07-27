package com.ymatou.messagebus.facade;

/**
 * 响应基类. <em>其所有子类必须有默认的构造函数</em>
 * 
 * @author tuwenjie
 *
 */
public class BaseResponse extends PrintFriendliness {

    private static final long serialVersionUID = -5719901720924490738L;

    private ErrorCode errorCode;

    private String errorMessage;

    private boolean isSuccess;

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        if (errorCode == null)
            return 0;
        else
            return errorCode.getCode();
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
