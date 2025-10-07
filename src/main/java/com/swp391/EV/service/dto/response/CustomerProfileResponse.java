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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {

    // Customer fields
    private UUID id;
    private String customerCode;
    private LocalDate dateOfBirth;
    private OffsetDateTime subscriptionExpiry;
    private BigDecimal totalSpent;
    private OffsetDateTime createdAt;

    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private boolean isActive;
    private boolean emailVerified;
    private OffsetDateTime lastLogin;
    private OffsetDateTime userCreatedAt;
    private OffsetDateTime userUpdatedAt;
}
