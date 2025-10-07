package com.swp391.EV.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private Boolean isActive;
}
