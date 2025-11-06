package com.swp391.EV.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private String role; // "STAFF" or "TECHNICIAN"
    private UUID serviceCenterId;
    private String specialization;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    private BigDecimal salary;
}
