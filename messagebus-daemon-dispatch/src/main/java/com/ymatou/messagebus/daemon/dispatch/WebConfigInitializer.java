/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.daemon.dispatch;

import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.remoting.http.servlet.BootstrapListener;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;

@Configuration
public class WebConfigInitializer extends SpringBootServletInitializer {

    @Bean
    public ServletContextListener bootstrapListener() {

        return new BootstrapListener();
    }

    @Bean
    public Servlet dispatcherServlet() {
        return new DispatcherServlet();
    }
}
