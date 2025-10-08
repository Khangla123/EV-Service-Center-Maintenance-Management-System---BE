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
    private UUID serviceCenterId;
    private String serviceCenterName;
    private UUID servicePackageId;
    private String servicePackageName;
    private LocalDateTime appointmentDate;
    private ServiceAppointment.AppointmentStatus status;
    private String notes;
    private LocalDateTime estimatedCompletion;
    private LocalDateTime actualCompletion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
