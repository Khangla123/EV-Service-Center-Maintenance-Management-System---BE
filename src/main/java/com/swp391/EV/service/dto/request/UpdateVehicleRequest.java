package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateVehicleRequest {
    private UUID vehicleModelId; // Thay đổi loại xe
    private String licensePlate;
    private String color;
    private LocalDate purchaseDate;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
}
