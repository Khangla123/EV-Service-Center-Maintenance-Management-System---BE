package com.swp391.EV.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffProfileRequest {
    private String fullName;
    private String phone;
    private String address;
    private String specialization;
}

