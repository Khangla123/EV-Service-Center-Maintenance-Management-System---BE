package com.swp391.EV.service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
    private String fullName;
}