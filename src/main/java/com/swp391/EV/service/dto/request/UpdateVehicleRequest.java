package com.swp391.EV.service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateVehicleRequest {
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
}
