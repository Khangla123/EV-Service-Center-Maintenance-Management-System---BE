package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartRepository extends JpaRepository<Part, UUID> {

    Optional<Part> findByPartCode(String partCode);

    List<Part> findByIsActiveTrue();

    @Query("SELECT p FROM Part p WHERE p.stockQuantity < p.minStockLevel AND p.isActive = true")
    List<Part> findLowStockParts();

    List<Part> findByCategory(String category);

    List<Part> findByServiceCenterId(UUID serviceCenterId);
}

