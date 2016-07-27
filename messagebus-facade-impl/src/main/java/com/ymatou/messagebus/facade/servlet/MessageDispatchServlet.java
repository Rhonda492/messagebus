/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.messagebus.facade.task.MessageDispatchTask;


/**
 * 消息分发定时任务
 * 
 * @author wangxudong 2016年7月27日 下午4:48:31
 *
 */
public class MessageDispatchServlet extends HttpServlet {
    private static Logger logger = LoggerFactory.getLogger(MessageDispatchServlet.class);

    /**
     * 序列化版本
     */
    private static final long serialVersionUID = 1L;

    // 定时器
    private Timer timer = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            start();
            logger.info("message dispatch servlet init.");
        } catch (Exception ex) {
            logger.error("message dispatch task start failed", ex);
            throw new ServletException("message dispatch task start failed", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out;
        resp.setContentType("text/html;charset=UTF-8");
        out = resp.getWriter();

        String path = req.getPathInfo();
        out.print("dispatch task control: " + path + "<br/>");

        String result = execute(path);

        out.print(result);
        out.close();
    }

    /**
     * 执行路径对应的命令
     * 
     * @param path
     * @return
     */
    private String execute(String path) {
        String command = "status";
        if (!StringUtils.isEmpty(path)) {
            command = path.trim();
            if (path.startsWith("/")) {
                command = command.substring(1);
            }
        }

        try {
            switch (command) {
                case "stop":
                    return stop();
                case "start":
                    return start();
                default:
                    return "invalid command.";
            }
        } catch (Exception e) {
            logger.error("execute compensate task control failed.", e);
            return "execute fail with ex: " + e.getMessage();
        }
    }

    /**
     * 停止任务
     * 
     * @param out
     */
    private String stop() throws Exception {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            return "stop success!";
        } else {
            return "task allready stop.";
        }
    }

    /**
     * 启动任务
     * 
     * @param out
     */
    private String start() throws Exception {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new MessageDispatchTask(), 0, 1000 * 1);
            return "start success!";
        } else {
            return "task allready start.";
        }
    }
}
