package com.swp391.EV.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {

    private String vnpUrl;
    private String returnUrl;
    private String tmnCode;
    private String secretKey;
    private String version;
    private String command;
    private String orderType;
    private int timeoutMinutes;
}

