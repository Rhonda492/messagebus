/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.rest;

/**
 * 系统消息接口
 * 
 * @author wangxudong 2016年5月22日 下午10:07:53
 *
 */
public interface SystemResource {
    /**
     * 返回系统状态
     * 
     * @return
     */
    String status();

    /**
     * 返回系统版本
     * 
     * @return
     */
    String version();
}
