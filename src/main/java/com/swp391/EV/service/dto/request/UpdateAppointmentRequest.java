package com.swp391.EV.service.dto.request;

import com.swp391.EV.service.model.ServiceAppointment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateAppointmentRequest {
    private LocalDateTime appointmentDate;
    private ServiceAppointment.AppointmentStatus status;
    private String notes;
    private LocalDateTime estimatedCompletion;
}
