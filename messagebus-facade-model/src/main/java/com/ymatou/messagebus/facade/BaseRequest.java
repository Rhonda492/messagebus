package com.ymatou.messagebus.facade;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * 请求基类
 * @author wangxudong
 *
 */
public class BaseRequest extends PrintFriendliness {

	/**
	 * 序列化版本
	 */
	private static final long serialVersionUID = 1995776180594622716L;
	
	/**
	 * 数据验证器
	 */
	private static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	/**
	 * 请求Id
	 */
	private String requestId;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * 验证数据有效性
	 */
	public void validate(){
		StringBuilder errorMsgs = new StringBuilder();
		Set<ConstraintViolation<BaseRequest>> violations = VALIDATOR.validate(this);
		
		if(violations != null && violations.size() > 0){
			for (ConstraintViolation<BaseRequest> violation : violations) {
				errorMsgs.append(violation.getMessage()).append("|");
			}
			throw new IllegalArgumentException(errorMsgs.substring(0, errorMsgs.length() - 1));
		}
	}
}
