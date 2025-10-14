package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, UUID> {
    Optional<VehicleModel> findByManufacturerAndModelAndYear(String manufacturer, String model, Integer year);
}

