package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCustomerRequest {
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
}
