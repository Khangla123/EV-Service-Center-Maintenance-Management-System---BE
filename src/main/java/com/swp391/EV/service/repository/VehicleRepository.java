package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByVin(String vin);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByCustomerId(UUID customerId);

    @Query("SELECT v FROM Vehicle v WHERE v.customer.id = :customerId AND v.isActive = true")
    List<Vehicle> findActiveVehiclesByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT v FROM Vehicle v WHERE v.manufacturer LIKE %:manufacturer%")
    List<Vehicle> findByManufacturerContaining(@Param("manufacturer") String manufacturer);

    @Query("SELECT v FROM Vehicle v WHERE v.model LIKE %:model%")
    List<Vehicle> findByModelContaining(@Param("model") String model);

    List<Vehicle> findByIsActiveTrue();
}
