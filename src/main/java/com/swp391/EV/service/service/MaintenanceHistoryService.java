package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.MaintenanceHistoryFilterRequest;
import com.swp391.EV.service.dto.response.MaintenanceHistoryResponse;
import com.swp391.EV.service.dto.response.MaintenanceHistoryStatisticsResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.ServiceAppointment;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.ServiceAppointmentRepository;
import com.swp391.EV.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceHistoryService {

    private final ServiceAppointmentRepository serviceAppointmentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MaintenanceHistoryStatisticsResponse getMaintenanceHistory(MaintenanceHistoryFilterRequest filter) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // Get maintenance history based on filters
        List<ServiceAppointment> appointments = getFilteredAppointments(customer.getId(), filter);

        // Map to response
        List<MaintenanceHistoryResponse> historyList = appointments.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        // Calculate statistics
        int totalMaintenances = historyList.size();
        BigDecimal totalCost = historyList.stream()
                .map(MaintenanceHistoryResponse::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageCost = totalMaintenances > 0
                ? totalCost.divide(BigDecimal.valueOf(totalMaintenances), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MaintenanceHistoryStatisticsResponse.builder()
                .totalMaintenances(totalMaintenances)
                .totalCost(totalCost)
                .averageCost(averageCost)
                .maintenanceHistory(historyList)
                .build();
    }

    private List<ServiceAppointment> getFilteredAppointments(java.util.UUID customerId, MaintenanceHistoryFilterRequest filter) {
        boolean hasVehicle = filter.getVehicleId() != null;
        boolean hasDateRange = filter.getFromDate() != null && filter.getToDate() != null;

        if (hasVehicle && hasDateRange) {
            LocalDateTime fromDateTime = filter.getFromDate().atStartOfDay();
            LocalDateTime toDateTime = filter.getToDate().atTime(LocalTime.MAX);
            return serviceAppointmentRepository.findMaintenanceHistoryByCustomerIdAndVehicleIdAndDateRange(
                    customerId, filter.getVehicleId(), fromDateTime, toDateTime);
        } else if (hasVehicle) {
            return serviceAppointmentRepository.findMaintenanceHistoryByCustomerIdAndVehicleId(
                    customerId, filter.getVehicleId());
        } else if (hasDateRange) {
            LocalDateTime fromDateTime = filter.getFromDate().atStartOfDay();
            LocalDateTime toDateTime = filter.getToDate().atTime(LocalTime.MAX);
            return serviceAppointmentRepository.findMaintenanceHistoryByCustomerIdAndDateRange(
                    customerId, fromDateTime, toDateTime);
        } else {
            return serviceAppointmentRepository.findMaintenanceHistoryByCustomerId(customerId);
        }
    }

    private MaintenanceHistoryResponse mapToHistoryResponse(ServiceAppointment appointment) {
        String vehicleModel = appointment.getVehicle() != null && appointment.getVehicle().getVehicleModel() != null
                ? appointment.getVehicle().getVehicleModel().getModel()
                : "N/A";

        String licensePlate = appointment.getVehicle() != null
                ? appointment.getVehicle().getLicensePlate()
                : "N/A";

        Integer mileage = appointment.getVehicle() != null
                ? appointment.getVehicle().getMileage()
                : 0;

        String serviceTitle = appointment.getServicePackage() != null
                ? appointment.getServicePackage().getName()
                : "Dịch vụ bảo dưỡng";

        // Calculate next maintenance date (e.g., 6 months from service date)
        LocalDateTime nextMaintenanceDate = appointment.getActualCompletion() != null
                ? appointment.getActualCompletion().plusMonths(6)
                : appointment.getAppointmentDate().plusMonths(6);

        // Determine if inspection passed based on status
        Boolean inspectionPassed = appointment.getStatus() == ServiceAppointment.AppointmentStatus.COMPLETED;

        return MaintenanceHistoryResponse.builder()
                .appointmentId(appointment.getId())
                .serviceTitle(serviceTitle)
                .vehicleModel(vehicleModel)
                .licensePlate(licensePlate)
                .mileage(mileage)
                .totalAmount(appointment.getServicePackage() != null ? appointment.getServicePackage().getPrice() : BigDecimal.ZERO)
                .serviceDate(appointment.getAppointmentDate())
                .nextMaintenanceDate(nextMaintenanceDate)
                .status(appointment.getStatus().name())
                .inspectionPassed(inspectionPassed)
                .build();
    }
}
