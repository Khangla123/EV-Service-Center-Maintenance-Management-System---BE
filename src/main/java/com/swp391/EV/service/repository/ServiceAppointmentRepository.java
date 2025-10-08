package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.ServiceAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceAppointmentRepository extends JpaRepository<ServiceAppointment, UUID> {

    List<ServiceAppointment> findByCustomerId(UUID customerId);

    List<ServiceAppointment> findByStatus(ServiceAppointment.AppointmentStatus status);

    @Query("SELECT sa FROM ServiceAppointment sa WHERE sa.appointmentDate BETWEEN :startDate AND :endDate")
    List<ServiceAppointment> findByAppointmentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sa FROM ServiceAppointment sa WHERE sa.serviceCenter.id = :serviceCenterId")
    List<ServiceAppointment> findByServiceCenterId(@Param("serviceCenterId") UUID serviceCenterId);
}
