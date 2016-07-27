package com.ymatou.messagebus.infrastructure.serialize;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public JacksonConfig() {
        objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(new BigCamelPropertyNamingStrategy());

        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        // 解析.NET客戶端传入的时间格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        objectMapper.setDateFormat(dateFormat);

        // 设置默认的时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}
