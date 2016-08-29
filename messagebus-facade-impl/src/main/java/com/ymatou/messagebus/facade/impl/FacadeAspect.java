package com.ymatou.messagebus.facade.impl;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.ymatou.messagebus.facade.BaseRequest;
import com.ymatou.messagebus.facade.BaseResponse;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;

/**
 * Facade实现方法的AOP.
 * 实现与业务无关的通用操作。
 * 1，日志
 * 2，异常处理等
 * 
 * @author tuwenjie
 *
 */
@Aspect
@Component
public class FacadeAspect {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(FacadeAspect.class);

    @Pointcut("execution(* com.ymatou.messagebus.facade.*Facade.*(*)) && args(req)")
    public void executAccountFacade(BaseRequest req) {};

    @Around("executAccountFacade(req)")
    public Object aroundFacadeExecution(ProceedingJoinPoint joinPoint, BaseRequest req)
            throws InstantiationException, IllegalAccessException {

        Logger logger = DEFAULT_LOGGER;

        if (req == null) {
            logger.error("Recv: null");
            return builErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, "request is null");
        }

        long startTime = System.currentTimeMillis();

        if (StringUtils.isEmpty(req.getRequestId())) {
            req.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        // log日志配有"logPrefix"占位符
        MDC.put("logPrefix", getRequestFlag(req));

        logger.info("Recv:" + req);

        Object resp = null;

        try {

            req.validate();

            resp = joinPoint.proceed(new Object[] {req});

        } catch (IllegalArgumentException e) {
            resp = builErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, e.getLocalizedMessage());
            logger.error("Invalid request: {}", req, e);

        } catch (BizException e) {
            resp = builErrorResponse(joinPoint, e.getErrorCode(),
                    e.getErrorCode().getMessage() + "|" + e.getLocalizedMessage());
            logger.warn("Failed to execute request: {}, Error:{}", req.getRequestId(),
                    e.getErrorCode() + "|" + e.getErrorCode().getMessage() + "|" + e.getLocalizedMessage());

        } catch (Throwable e) {
            resp = builErrorResponse(joinPoint, ErrorCode.UNKNOWN, e.getLocalizedMessage());
            logger.error("Unknown error in executing request:{}", req, e);
        } finally {
            logger.info("Resp:" + resp);
            // MDC.clear();
        }

        long consumedTime = System.currentTimeMillis() - startTime;

        logger.info("Finished {}, Consumed:{}ms", getRequestFlag(req), consumedTime);
        return resp;
    }


    private BaseResponse builErrorResponse(ProceedingJoinPoint joinPoint, ErrorCode errorCode, String errorMsg)
            throws InstantiationException, IllegalAccessException {

        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        BaseResponse resp = (BaseResponse) ms.getReturnType().newInstance();
        resp.setErrorCode(errorCode);
        resp.setErrorMessage(errorMsg);
        return resp;

    }

    private String getRequestFlag(BaseRequest req) {
        return req.getClass().getSimpleName() + "|" + req.getRequestId();
    }
}
