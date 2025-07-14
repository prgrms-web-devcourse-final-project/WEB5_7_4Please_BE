package com.deal4u.fourplease.domain.payment.config;

import feign.RequestInterceptor;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TossFeignConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            requestTemplate.header("Authorization", "Basic " + encodedKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
