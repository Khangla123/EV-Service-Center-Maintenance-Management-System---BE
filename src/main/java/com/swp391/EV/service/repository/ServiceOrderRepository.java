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

    // NOTE: Status removed from ServiceOrder - query appointment.status instead
    // List<ServiceOrder> findByStatus(ServiceOrder.ServiceStatus status);

    // Query by technicianUserId (UUID column in service_orders table)
    @Query("SELECT so FROM ServiceOrder so WHERE so.technicianUserId = :technicianId")
    List<ServiceOrder> findByTechnicianId(@Param("technicianId") UUID technicianId);

    @Query("SELECT so FROM ServiceOrder so " +
           "WHERE so.appointment.id = :appointmentId")
    Optional<ServiceOrder> findByAppointmentId(@Param("appointmentId") UUID appointmentId);

    @Query("SELECT so FROM ServiceOrder so " +
           "WHERE so.appointment.customer.id = :customerId")
    List<ServiceOrder> findByCustomerId(@Param("customerId") UUID customerId);
    
    // Fetch ServiceOrder with all necessary relationships for invoice creation
    @Query("SELECT so FROM ServiceOrder so " +
           "LEFT JOIN FETCH so.appointment a " +
           "LEFT JOIN FETCH a.customer c " +
           "LEFT JOIN FETCH a.vehicle v " +
           "LEFT JOIN FETCH so.technician t " +
           "LEFT JOIN FETCH t.user tu " +
           "WHERE so.id = :id")
    Optional<ServiceOrder> findByIdWithRelations(@Param("id") UUID id);
    
    List<ServiceOrder> findAll();
}
