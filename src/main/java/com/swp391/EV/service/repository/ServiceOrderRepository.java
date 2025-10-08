package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    Optional<ServiceOrder> findByOrderCode(String orderCode);

    List<ServiceOrder> findByStatus(ServiceOrder.ServiceStatus status);

    @Query("SELECT so FROM ServiceOrder so WHERE so.technician.id = :technicianId")
    List<ServiceOrder> findByTechnicianId(@Param("technicianId") UUID technicianId);

    @Query("SELECT so FROM ServiceOrder so WHERE so.appointment.id = :appointmentId")
    Optional<ServiceOrder> findByAppointmentId(@Param("appointmentId") UUID appointmentId);

    @Query("SELECT so FROM ServiceOrder so WHERE so.appointment.customer.id = :customerId")
    List<ServiceOrder> findByCustomerId(@Param("customerId") UUID customerId);
}
