package com.swp391.EV.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {

    // VNPay Sandbox credentials (miễn phí cho test)
    private String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:3000/payment/callback"; // URL frontend nhận kết quả
    private String tmnCode = "YOUR_TMN_CODE"; // Lấy từ VNPay Sandbox
    private String secretKey = "YOUR_SECRET_KEY"; // Lấy từ VNPay Sandbox
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";

    // Timeout: 15 phút
    private int timeoutMinutes = 15;
}

