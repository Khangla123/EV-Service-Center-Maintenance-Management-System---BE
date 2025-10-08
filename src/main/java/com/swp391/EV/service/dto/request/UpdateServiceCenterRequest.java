package com.swp391.EV.service.dto.request;

import lombok.Data;

@Data
public class UpdateServiceCenterRequest {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String operatingHours;
    private Integer capacity;
    private Boolean isActive;
}
