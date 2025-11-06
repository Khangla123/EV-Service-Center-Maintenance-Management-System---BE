package com.swp391.EV.service.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerCreateRequest {

    // User fields
    private String username;
    private String email;
    private String password; // Add password field for signup
    private String fullName;
    private String phone;
    private String address;

    // Customer specific fields
    private String customerCode;
    private LocalDate dateOfBirth;
}
