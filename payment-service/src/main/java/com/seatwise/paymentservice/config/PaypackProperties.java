package com.seatwise.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "paypack")
public class PaypackProperties {
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String webhookSecret;
    private String webhookMode; // "development" or "production"
}