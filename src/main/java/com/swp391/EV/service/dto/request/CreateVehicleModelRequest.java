package com.swp391.EV.service.dto.request;

import lombok.Data;

@Data
public class CreateVehicleModelRequest {
    private String manufacturer;
    private String model;
    private Integer year;
    private Double batteryCapacity;
    private Integer rangeKm;
}

