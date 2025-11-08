package com.swp391.EV.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {

    private String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:3000/payment/result"; // URL frontend nhận kết quả
    // VNPay Sandbox Test Credentials (public demo)
    private String tmnCode = "DEMOV210";  // Demo merchant code
    private String secretKey = "GECKPDSQTMXVYSNSRHTFUZWARXNYAEPH"; // Demo secret key
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";

    private int timeoutMinutes = 15;
}

