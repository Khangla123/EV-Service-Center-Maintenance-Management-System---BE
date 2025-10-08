package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, UUID> {

    List<ServiceCenter> findByIsActiveTrue();

    @Query("SELECT sc FROM ServiceCenter sc WHERE sc.name LIKE %:name% AND sc.isActive = true")
    List<ServiceCenter> findByNameContainingAndActiveTrue(@Param("name") String name);

    @Query("SELECT sc FROM ServiceCenter sc WHERE sc.address LIKE %:address% AND sc.isActive = true")
    List<ServiceCenter> findByAddressContainingAndActiveTrue(@Param("address") String address);

    Optional<ServiceCenter> findByNameAndIsActiveTrue(String name);
}
