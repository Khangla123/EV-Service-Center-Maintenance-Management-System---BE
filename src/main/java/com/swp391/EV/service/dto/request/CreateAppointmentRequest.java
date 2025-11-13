package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateAppointmentRequest {
    private UUID customerId;
    private UUID vehicleId;
    private UUID serviceCenterId;
    private UUID servicePackageId;
    private String selectedPackages; // JSON array of selected package IDs
    private LocalDateTime appointmentDate;
    private String notes;
}
