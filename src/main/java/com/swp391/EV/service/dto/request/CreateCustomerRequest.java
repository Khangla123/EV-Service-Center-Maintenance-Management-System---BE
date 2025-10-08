package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCustomerRequest {
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
}
