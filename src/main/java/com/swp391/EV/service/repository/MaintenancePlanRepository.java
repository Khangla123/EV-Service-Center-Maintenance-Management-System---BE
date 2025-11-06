package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {
    
    /**
     * Find active maintenance plan by service package ID
     */
    @Query("SELECT mp FROM MaintenancePlan mp " +
           "WHERE mp.servicePackage.id = :servicePackageId " +
           "AND mp.isActive = true " +
           "ORDER BY mp.createdAt DESC")
    Optional<MaintenancePlan> findByServicePackageId(@Param("servicePackageId") UUID servicePackageId);
    
    /**
     * Find maintenance plan by service package ID and vehicle model ID
     */
    @Query("SELECT mp FROM MaintenancePlan mp " +
           "WHERE mp.servicePackage.id = :servicePackageId " +
           "AND (mp.vehicleModel.id = :vehicleModelId OR mp.vehicleModel IS NULL) " +
           "AND mp.isActive = true " +
           "ORDER BY mp.vehicleModel.id NULLS LAST, mp.createdAt DESC")
    Optional<MaintenancePlan> findByServicePackageIdAndVehicleModelId(
        @Param("servicePackageId") UUID servicePackageId,
        @Param("vehicleModelId") UUID vehicleModelId
    );
}
