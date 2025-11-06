package com.swp391.EV.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponse {
    private UUID id;
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private String staffCode;
    private UUID serviceCenterId;
    private String serviceCenterName;
    private String specialization;
    private LocalDate hireDate;
    private BigDecimal salary;
    private Boolean isAvailable;
    private Boolean isActive;
    private String currentStatus; // "AVAILABLE", "BUSY", "INACTIVE"
    private OffsetDateTime createdAt;
}

