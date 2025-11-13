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

    @Query("SELECT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.customer " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.serviceCenter " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "LEFT JOIN FETCH sa.technician t " +
           "LEFT JOIN FETCH t.user " +
           "WHERE sa.customer.id = :customerId " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findByCustomerId(@Param("customerId") UUID customerId);

    List<ServiceAppointment> findByStatus(ServiceAppointment.AppointmentStatus status);

    @Query("SELECT sa FROM ServiceAppointment sa WHERE sa.appointmentDate BETWEEN :startDate AND :endDate")
    List<ServiceAppointment> findByAppointmentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sa FROM ServiceAppointment sa WHERE sa.serviceCenter.id = :serviceCenterId")
    List<ServiceAppointment> findByServiceCenterId(@Param("serviceCenterId") UUID serviceCenterId);

    @Query("SELECT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.customer " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.serviceCenter " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "LEFT JOIN FETCH sa.technician t " +
           "LEFT JOIN FETCH t.user")
    List<ServiceAppointment> findAllWithDetails();

    @Query("SELECT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.customer " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.serviceCenter " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "LEFT JOIN FETCH sa.technician t " +
           "LEFT JOIN FETCH t.user " +
           "WHERE sa.technician.id = :technicianId " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findByTechnicianIdWithDetails(@Param("technicianId") UUID technicianId);

    // New queries for maintenance history
    @Query("SELECT DISTINCT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "WHERE sa.customer.id = :customerId " +
           "AND sa.status IN ('COMPLETED', 'IN_PROGRESS') " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findMaintenanceHistoryByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT DISTINCT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "WHERE sa.customer.id = :customerId " +
           "AND sa.vehicle.id = :vehicleId " +
           "AND sa.status IN ('COMPLETED', 'IN_PROGRESS') " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findMaintenanceHistoryByCustomerIdAndVehicleId(
            @Param("customerId") UUID customerId,
            @Param("vehicleId") UUID vehicleId);

    @Query("SELECT DISTINCT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "WHERE sa.customer.id = :customerId " +
           "AND sa.appointmentDate BETWEEN :fromDate AND :toDate " +
           "AND sa.status IN ('COMPLETED', 'IN_PROGRESS') " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findMaintenanceHistoryByCustomerIdAndDateRange(
            @Param("customerId") UUID customerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("SELECT DISTINCT sa FROM ServiceAppointment sa " +
           "LEFT JOIN FETCH sa.vehicle v " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH sa.servicePackage " +
           "WHERE sa.customer.id = :customerId " +
           "AND sa.vehicle.id = :vehicleId " +
           "AND sa.appointmentDate BETWEEN :fromDate AND :toDate " +
           "AND sa.status IN ('COMPLETED', 'IN_PROGRESS') " +
           "ORDER BY sa.appointmentDate DESC")
    List<ServiceAppointment> findMaintenanceHistoryByCustomerIdAndVehicleIdAndDateRange(
            @Param("customerId") UUID customerId,
            @Param("vehicleId") UUID vehicleId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT COUNT(*) FROM service_appointments sa " +
           "WHERE sa.technician_id = :technicianId " +
           "AND sa.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "AND DATE_TRUNC('hour', sa.appointment_date) = DATE_TRUNC('hour', CAST(:appointmentDate AS timestamp))",
           nativeQuery = true)
    long countConflictingAppointments(
            @Param("technicianId") UUID technicianId,
            @Param("appointmentDate") LocalDateTime appointmentDate);
}
