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
public class UpdateStaffRequest {
    private String fullName;
    private String phone;
    private String address;
    private UUID serviceCenterId;
    private String specialization;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    private BigDecimal salary;
}
