/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;

@Component
public class CompensateService {

    @Resource
    private MessageCompensateRepository messageCompensateRepository;


}
