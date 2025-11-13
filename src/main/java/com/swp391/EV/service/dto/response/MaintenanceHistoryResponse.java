package com.swp391.EV.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceHistoryResponse {
    private UUID appointmentId;
    private String serviceTitle;
    private String selectedPackageNames; // Comma-separated names of all selected packages
    private String vehicleModel;
    private String licensePlate;
    private Integer mileage;
    private BigDecimal totalAmount;
    private LocalDateTime serviceDate;
    private LocalDateTime nextMaintenanceDate;
    private String status;
    private Boolean inspectionPassed;
}

