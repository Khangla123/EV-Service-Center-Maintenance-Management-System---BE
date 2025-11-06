package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findByUserId(UUID userId);

    Optional<Staff> findByStaffCode(String staffCode);

    List<Staff> findByIsAvailable(Boolean isAvailable);

    List<Staff> findByServiceCenterId(UUID serviceCenterId);

    @Query("SELECT s FROM Staff s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.serviceCenter " +
           "WHERE UPPER(s.user.role) IN ('STAFF', 'TECHNICIAN')")
    List<Staff> findAllStaffAndTechnicians();
}

