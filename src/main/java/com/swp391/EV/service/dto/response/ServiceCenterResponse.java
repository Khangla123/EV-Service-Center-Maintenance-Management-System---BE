package com.swp391.EV.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ServiceCenterResponse {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String operatingHours;
    private Integer capacity;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
