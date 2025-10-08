package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateServiceOrderRequest {
    private UUID appointmentId;
    private UUID technicianId;
    private String notes;
}
