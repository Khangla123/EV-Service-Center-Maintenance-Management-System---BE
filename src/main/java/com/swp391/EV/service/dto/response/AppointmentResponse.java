package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.ServiceAppointment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private UUID vehicleId;
    private String vehicleLicensePlate;
    private String vehicleModel;
    private UUID serviceCenterId;
    private String serviceCenterName;
    private UUID servicePackageId;
    private String servicePackageName;
    private String selectedPackages; // JSON array of selected package IDs
    private String selectedPackageNames; // Comma-separated package names for display
    private UUID technicianId;
    private String technicianName;
    private LocalDateTime appointmentDate;
    private ServiceAppointment.AppointmentStatus status;
    private String notes;
    private LocalDateTime estimatedCompletion;
    private LocalDateTime actualCompletion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
