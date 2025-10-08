package com.swp391.EV.service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private boolean isActive;
    private boolean emailVerified;
    private OffsetDateTime lastLogin;
    private LocalDate dateOfBirth;
    private String customerCode;
    private OffsetDateTime subscriptionExpiry;
    private BigDecimal totalSpent;
    private OffsetDateTime createdAt; // Đã thay từ LocalDateTime thành OffsetDateTime
    private OffsetDateTime updatedAt; // Đã thay từ LocalDateTime thành OffsetDateTime
}
