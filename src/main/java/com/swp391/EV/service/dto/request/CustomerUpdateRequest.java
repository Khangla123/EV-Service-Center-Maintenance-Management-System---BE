package com.swp391.EV.service.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class CustomerUpdateRequest {

    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Boolean isActive;

    private String customerCode;
    private LocalDate dateOfBirth;
    private OffsetDateTime subscriptionExpiry;
}
