package com.swp391.EV.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VehicleModelResponse {
    private UUID id;
    private String manufacturer;
    private String model;
    private Integer year;
    private Double batteryCapacity;
    private Integer rangeKm;
    private String imageUrl;
    private LocalDateTime createdAt;
}

