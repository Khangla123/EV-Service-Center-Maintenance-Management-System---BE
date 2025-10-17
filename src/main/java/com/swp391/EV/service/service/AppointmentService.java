package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateAppointmentRequest;
import com.swp391.EV.service.dto.request.UpdateAppointmentRequest;
import com.swp391.EV.service.dto.response.AppointmentResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.*;
import com.swp391.EV.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    @Autowired
    private final ServiceAppointmentRepository appointmentRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final VehicleRepository vehicleRepository;
    @Autowired
    private final ServiceCenterRepository serviceCenterRepository;
    @Autowired
    private final ServicePackageRepository servicePackageRepository;
    @Autowired
    private final ModelMapper modelMapper;

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllWithDetails().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ServiceCenter serviceCenter = serviceCenterRepository.findById(request.getServiceCenterId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ServicePackage servicePackage = servicePackageRepository.findById(request.getServicePackageId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ServiceAppointment appointment = ServiceAppointment.builder()
                .customer(customer)
                .vehicle(vehicle)
                .serviceCenter(serviceCenter)
                .servicePackage(servicePackage)
                .appointmentDate(request.getAppointmentDate())
                .notes(request.getNotes())
                .status(ServiceAppointment.AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceAppointment savedAppointment = appointmentRepository.save(appointment);
        
        return convertToResponse(savedAppointment);
    }

    public AppointmentResponse getAppointmentById(UUID id) {
        ServiceAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return convertToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(UUID id, UpdateAppointmentRequest request) {
        ServiceAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        if (request.getEstimatedCompletion() != null) {
            appointment.setEstimatedCompletion(request.getEstimatedCompletion());
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        ServiceAppointment updatedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(updatedAppointment);
    }

    @Transactional
    public void deleteAppointment(UUID id) {
        ServiceAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        appointmentRepository.delete(appointment);
    }

    public List<AppointmentResponse> getAppointmentsByCustomerId(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAvailableTimeSlots() {
        // Logic to get available time slots
        return appointmentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Staff xác nhận lịch hẹn (PENDING -> CONFIRMED)
     */
    @Transactional
    public AppointmentResponse confirmAppointment(UUID appointmentId) {
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Kiểm tra trạng thái hiện tại
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Cập nhật trạng thái thành CONFIRMED
        appointment.setStatus(ServiceAppointment.AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());

        ServiceAppointment confirmedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(confirmedAppointment);
    }

    /**
     * Lấy danh sách appointments theo status
     */
    public List<AppointmentResponse> getAppointmentsByStatus(ServiceAppointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Staff hủy lịch hẹn
     */
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, String reason) {
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Cập nhật trạng thái thành CANCELLED
        appointment.setStatus(ServiceAppointment.AppointmentStatus.CANCELLED);
        if (reason != null && !reason.isEmpty()) {
            appointment.setNotes(appointment.getNotes() + " | Lý do hủy: " + reason);
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        ServiceAppointment cancelledAppointment = appointmentRepository.save(appointment);
        return convertToResponse(cancelledAppointment);
    }

    private AppointmentResponse convertToResponse(ServiceAppointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        
        // Handle customer with null safety
        if (appointment.getCustomer() != null) {
            try {
                response.setCustomerId(appointment.getCustomer().getId());
                response.setCustomerName(appointment.getCustomer().getFullName());
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setCustomerId(null);
                response.setCustomerName("Unknown");
            }
        }
        
        if (appointment.getVehicle() != null) {
            try {
                UUID vehicleId = appointment.getVehicle().getId();
                response.setVehicleId(vehicleId);
                response.setVehicleLicensePlate(appointment.getVehicle().getLicensePlate());

                if (appointment.getVehicle().getVehicleModel() != null) {
                    response.setVehicleModel(appointment.getVehicle().getVehicleModel().getModel());
                }
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setVehicleId(null);
            }
        }
        
        if (appointment.getServiceCenter() != null) {
            try {
                response.setServiceCenterId(appointment.getServiceCenter().getId());
                response.setServiceCenterName(appointment.getServiceCenter().getName());
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setServiceCenterId(null);
            }
        }
        
        if (appointment.getServicePackage() != null) {
            try {
                response.setServicePackageId(appointment.getServicePackage().getId());
                response.setServicePackageName(appointment.getServicePackage().getName());
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setServicePackageId(null);
            }
        }
        
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setEstimatedCompletion(appointment.getEstimatedCompletion());
        response.setActualCompletion(appointment.getActualCompletion());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}
