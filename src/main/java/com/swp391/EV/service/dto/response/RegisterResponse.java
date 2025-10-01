package com.swp391.EV.service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String message;
    private LoginResponse loginInfo;
}
