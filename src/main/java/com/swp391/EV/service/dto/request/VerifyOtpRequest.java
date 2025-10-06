package com.swp391.EV.service.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otpCode;
}
