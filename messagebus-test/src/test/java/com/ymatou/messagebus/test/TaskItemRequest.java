/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test;

import java.util.Date;
import java.util.Map;

/**
 * 定时任务请求体
 * 
 * @author wangxudong 2016年8月10日 下午3:44:44
 *
 */
public class TaskItemRequest {
    private String taskName;

    private String id;

    private Date taskTime;

    private Map<String, String> parameters;

    /**
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the taskTime
     */
    public Date getTaskTime() {
        return taskTime;
    }

    /**
     * @param taskTime the taskTime to set
     */
    public void setTaskTime(Date taskTime) {
        this.taskTime = taskTime;
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * 获取一个默认的请求实例
     * 
     * @return
     */
    public static TaskItemRequest newInstance() {
        TaskItemRequest taskItemRequest = new TaskItemRequest();
        taskItemRequest.setId("9999");
        taskItemRequest.setTaskName("Trading.CancelOrder");
        taskItemRequest.setTaskTime(new Date());

        return taskItemRequest;
    }
}
