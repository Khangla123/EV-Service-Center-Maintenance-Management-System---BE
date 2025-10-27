package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicle_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "battery_capacity")
    private Double batteryCapacity;

    @Column(name = "range_km")
    private Integer rangeKm;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

