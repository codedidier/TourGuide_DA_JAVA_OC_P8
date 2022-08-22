package com.codedidier.gpsprovider.config;

import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GpsConfig {
    @Bean
    public GpsUtil getGpsUtil() {
        return new GpsUtil();
    }
}
