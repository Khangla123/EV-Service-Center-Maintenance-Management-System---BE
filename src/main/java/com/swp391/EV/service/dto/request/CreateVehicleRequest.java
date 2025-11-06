package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateVehicleRequest {
    private UUID customerId;
    private UUID vehicleModelId; // Liên kết với VehicleModel
    private String vin;
    private String licensePlate;
    private String color;
    private LocalDate purchaseDate;
    private Integer mileage;
}
