package com.swp391.EV.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.EV.service.dto.request.MaintenanceHistoryFilterRequest;
import com.swp391.EV.service.dto.response.MaintenanceHistoryResponse;
import com.swp391.EV.service.dto.response.MaintenanceHistoryStatisticsResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.ServiceAppointment;
import com.swp391.EV.service.model.ServicePackage;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.ServiceAppointmentRepository;
import com.swp391.EV.service.repository.ServicePackageRepository;
import com.swp391.EV.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceHistoryService {

    @Autowired
    private final ServiceAppointmentRepository serviceAppointmentRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ServicePackageRepository servicePackageRepository;
    @Autowired
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public MaintenanceHistoryStatisticsResponse getMaintenanceHistory(MaintenanceHistoryFilterRequest filter) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        Customer customer;
        
        // Check if principalName is UUID (user ID) or email
        try {
            // Try to parse as UUID first
            java.util.UUID userId = java.util.UUID.fromString(principalName);
            
            // Find customer by user ID
            customer = customerRepository.findByUserId(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        } catch (IllegalArgumentException e) {
            // Not a UUID, treat as email
            
            // Try to find customer directly by email first
            customer = customerRepository.findByEmail(principalName)
                    .orElseGet(() -> {
                        // Fallback: try to find by userId
                        User user = userRepository.findByEmail(principalName)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                        return customerRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
                    });
        }

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
                .filter(java.util.Objects::nonNull)
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

        // Parse selected packages and generate comma-separated package names
        String selectedPackageNames = null;
        BigDecimal totalAmount = appointment.getServicePackage() != null 
                ? appointment.getServicePackage().getPrice() 
                : BigDecimal.ZERO;
        
        if (appointment.getSelectedPackages() != null && !appointment.getSelectedPackages().trim().isEmpty()) {
            try {
                List<String> packageIdStrings = objectMapper.readValue(
                        appointment.getSelectedPackages(),
                        new TypeReference<List<String>>() {}
                );
                
                List<String> packageNames = packageIdStrings.stream()
                        .map(idStr -> {
                            try {
                                UUID packageId = UUID.fromString(idStr);
                                ServicePackage pkg = servicePackageRepository.findById(packageId).orElse(null);
                                if (pkg != null) {
                                    return pkg.getName();
                                }
                                return "Unknown Package";
                            } catch (IllegalArgumentException e) {
                                return "Invalid Package ID";
                            }
                        })
                        .collect(Collectors.toList());
                
                // Calculate total with selected packages
                for (String idStr : packageIdStrings) {
                    try {
                        UUID packageId = UUID.fromString(idStr);
                        ServicePackage pkg = servicePackageRepository.findById(packageId).orElse(null);
                        if (pkg != null) {
                            totalAmount = totalAmount.add(pkg.getPrice());
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid package IDs
                    }
                }
                
                selectedPackageNames = String.join(", ", packageNames);
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        // Calculate next maintenance date (e.g., 6 months from service date)
        LocalDateTime nextMaintenanceDate = appointment.getActualCompletion() != null
                ? appointment.getActualCompletion().plusMonths(6)
                : appointment.getAppointmentDate().plusMonths(6);

        // Determine if inspection passed based on status
        // COMPLETED = true (đã kiểm tra và đạt)
        // IN_PROGRESS = null (chưa kiểm tra xong)
        // Other statuses = false (không đạt)
        Boolean inspectionPassed;
        if (appointment.getStatus() == ServiceAppointment.AppointmentStatus.COMPLETED) {
            inspectionPassed = true;
        } else if (appointment.getStatus() == ServiceAppointment.AppointmentStatus.IN_PROGRESS) {
            inspectionPassed = null; // Chưa kiểm tra
        } else {
            inspectionPassed = false;
        }

        return MaintenanceHistoryResponse.builder()
                .appointmentId(appointment.getId())
                .serviceTitle(serviceTitle)
                .selectedPackageNames(selectedPackageNames)
                .vehicleModel(vehicleModel)
                .licensePlate(licensePlate)
                .mileage(mileage)
                .totalAmount(totalAmount)
                .serviceDate(appointment.getAppointmentDate())
                .nextMaintenanceDate(nextMaintenanceDate)
                .status(appointment.getStatus().name())
                .inspectionPassed(inspectionPassed)
                .build();
    }
}
