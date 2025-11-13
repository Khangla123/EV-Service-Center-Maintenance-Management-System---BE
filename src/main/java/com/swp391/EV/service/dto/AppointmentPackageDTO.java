package com.swp391.EV.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentPackageDTO {
    private UUID id;
    private UUID packageId;
    private String packageName;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
}
