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
    private UUID vehicleModelId; // ID của VehicleModel
    private String vin;
    private String licensePlate;
    private String manufacturer; // Từ VehicleModel
    private String model; // Từ VehicleModel
    private Integer year; // Từ VehicleModel
    private String color;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiration;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private Double batteryCapacity; // Từ VehicleModel
    private Integer rangeKm; // Từ VehicleModel
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
