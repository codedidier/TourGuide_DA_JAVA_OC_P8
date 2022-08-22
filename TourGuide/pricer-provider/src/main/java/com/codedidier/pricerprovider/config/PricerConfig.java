package com.codedidier.pricerprovider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tripPricer.TripPricer;

@Configuration
public class PricerConfig {
    @Bean
    public TripPricer getTripPricer() {
        return new TripPricer();
    }
}
