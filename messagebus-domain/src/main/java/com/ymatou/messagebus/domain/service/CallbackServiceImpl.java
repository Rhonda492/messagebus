/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.domain.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.domain.model.AppConfig;
import com.ymatou.messagebus.domain.model.CallbackConfig;
import com.ymatou.messagebus.domain.model.Message;
import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.domain.model.MessageConfig;
import com.ymatou.messagebus.domain.repository.AppConfigRepository;
import com.ymatou.messagebus.domain.repository.MessageCompensateRepository;
import com.ymatou.messagebus.domain.repository.MessageRepository;
import com.ymatou.messagebus.facade.enums.MessageCompensateSourceEnum;
import com.ymatou.messagebus.facade.enums.MessageNewStatusEnum;
import com.ymatou.messagebus.infrastructure.net.HttpClientUtil;
import com.ymatou.messagebus.infrastructure.net.HttpResult;
import com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService;

/**
 * 回调服务
 * 
 * @author wangxudong 2016年8月5日 下午7:03:35
 *
 */
@Component
public class CallbackServiceImpl implements CallbackService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

    private CloseableHttpClient httpClient;

    @Resource
    private AppConfigRepository appConfigRepository;

    @Resource
    private MessageCompensateRepository messageCompensateRepository;

    @Resource
    private MessageRepository messageRepository;

    /*
     * (non-Javadoc)
     * 
     * @see com.ymatou.messagebus.infrastructure.rabbitmq.CallbackService#invoke(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void invoke(String exchange, String queue, String message, String messageId) {
        AppConfig appConfig = appConfigRepository.getAppConfig(exchange);
        MessageConfig messageConfig = appConfig.getMessageConfigByAppCode(queue);
        List<CallbackConfig> callbackCfgList = messageConfig.getCallbackCfgList();
        Message messageInfo =
                messageRepository.getByMessageId(appConfig.getAppId(), messageConfig.getCode(), messageId);

        for (CallbackConfig callbackConfig : callbackCfgList) {
            if (callbackConfig.getEnable() == null || callbackConfig.getEnable() == true) {
                String contentType = callbackConfig.getContentType();
                if (contentType == null) {
                    contentType = "application/json";
                }

                try {
                    HttpResult result =
                            HttpClientUtil.sendPost(callbackConfig.getUrl(), message, contentType, null, httpClient);

                    logger.info("http result:{}", result);
                    // Thread.sleep(1000 * 5);

                    if (isCallbackSuccess(result)) {
                        messageRepository.updateMessageStatusAndPublishTime(appConfig.getAppId(),
                                messageConfig.getCode(), messageId,
                                MessageNewStatusEnum.Success.code());
                    } else {
                        publishToCompensate(appConfig, messageInfo);
                        messageRepository.updateMessageStatusAndPublishTime(appConfig.getAppId(),
                                messageConfig.getCode(), messageId,
                                MessageNewStatusEnum.DispatchToCompensate.code());
                    }

                } catch (Exception e) {
                    logger.error("consumer callback faild, callbackKey:" + callbackConfig.getCallbackKey(), e);

                }
            }
        }
    }

    /**
     * 判断回调是否成功
     * 
     * @param result
     * @return
     */
    private boolean isCallbackSuccess(HttpResult result) {
        if (result.getStatusCode() == 200 && result.getBody() != null
                && (result.getBody().equalsIgnoreCase("ok") || result.getBody().equalsIgnoreCase("\"ok\""))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发布消息到补偿库
     * 
     * @param appConfig
     * @param message
     */
    private void publishToCompensate(AppConfig appConfig, Message message) {
        try {
            MessageCompensate messageCompensate = MessageCompensate.from(appConfig, message);
            messageCompensate.setSource(MessageCompensateSourceEnum.Dispatch.code());
            messageCompensateRepository.insert(messageCompensate);
        } catch (Exception ex) {
            logger.error("publish to mongodb failed with appcode:" + message.getAppCode(), ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

}
