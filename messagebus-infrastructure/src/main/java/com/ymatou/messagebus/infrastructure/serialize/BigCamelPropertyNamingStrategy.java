package com.ymatou.messagebus.infrastructure.serialize;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

/**
 * Json属性序列化成大写驼峰格式
 * 
 * @author wangxudong
 *
 */
public class BigCamelPropertyNamingStrategy extends PropertyNamingStrategy {

    /**
     * 序列化版本
     */
    private static final long serialVersionUID = 5481923680145129858L;

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        String methodName = method.getName();
        if (methodName.startsWith("get"))
            return methodName.substring(3);
        else if (methodName.startsWith("is"))
            return "Is" + methodName.substring(2);
        else
            return methodName;
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        String methodName = method.getName();
        if (methodName.startsWith("set"))
            return methodName.substring(3);
        else if (methodName.startsWith("is"))
            return methodName.substring(2);
        else
            return methodName;
    }
}
