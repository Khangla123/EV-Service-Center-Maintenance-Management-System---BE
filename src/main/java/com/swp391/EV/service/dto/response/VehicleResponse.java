package com.swp391.EV.service.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VehicleResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String vin;
    private String licensePlate;
    private String manufacturer;
    private String model;
    private Integer year;
    private String color;
    private LocalDate purchaseDate;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private Double batteryCapacity;
    private Integer rangeKm;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
