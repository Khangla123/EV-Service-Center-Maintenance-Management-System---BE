package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateAppointmentRequest;
import com.swp391.EV.service.dto.request.CreateInvoiceRequest;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final StaffRepository staffRepository;
    @Autowired
    private final ServiceOrderRepository serviceOrderRepository;
    @Autowired
    private final ModelMapper modelMapper;
    
    // Inject InvoiceService để tự động tạo invoice
    @Autowired
    private InvoiceService invoiceService;

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
                .selectedPackages(request.getSelectedPackages()) // ✅ Save selected packages JSON
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
            ServiceAppointment.AppointmentStatus oldStatus = appointment.getStatus();
            ServiceAppointment.AppointmentStatus newStatus = request.getStatus();
            
            appointment.setStatus(newStatus);
            
            // Tự động set estimatedCompletion khi chuyển sang IN_PROGRESS
            if (newStatus == ServiceAppointment.AppointmentStatus.IN_PROGRESS 
                && oldStatus != ServiceAppointment.AppointmentStatus.IN_PROGRESS
                && appointment.getEstimatedCompletion() == null) {
                
                // Tính thời gian dự kiến hoàn thành dựa trên service package duration
                LocalDateTime estimatedTime = LocalDateTime.now();
                if (appointment.getServicePackage() != null 
                    && appointment.getServicePackage().getDurationMinutes() != null) {
                    estimatedTime = estimatedTime.plusMinutes(appointment.getServicePackage().getDurationMinutes());
                } else {
                    // Mặc định 120 phút (2 giờ) nếu không có duration
                    estimatedTime = estimatedTime.plusMinutes(120);
                }
                appointment.setEstimatedCompletion(estimatedTime);
            }
            
            // Set actualCompletion khi hoàn thành
            if (newStatus == ServiceAppointment.AppointmentStatus.COMPLETED 
                && appointment.getActualCompletion() == null) {
                appointment.setActualCompletion(LocalDateTime.now());
                
                // TỰ ĐỘNG TẠO INVOICE KHI HOÀN THÀNH - DISABLED
                // Staff sẽ tạo invoice thủ công thay vì tự động
                // try {
                //     createInvoiceForCompletedAppointment(appointment);
                // } catch (Exception e) {
                //     // Log lỗi nhưng không làm fail toàn bộ transaction
                //     System.err.println("Failed to auto-create invoice for appointment " + appointment.getId() + ": " + e.getMessage());
                // }
            }
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        if (request.getTechnicianId() != null) {
            Staff technician = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            appointment.setTechnician(technician);
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
     * Lấy danh sách appointments theo technician ID
     */
    public List<AppointmentResponse> getAppointmentsByTechnicianId(UUID technicianId) {
        // Use dedicated query to filter by technician ID at database level
        return appointmentRepository.findByTechnicianIdWithDetails(technicianId).stream()
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

    /**
     * Technician bắt đầu công việc - chuyển từ ASSIGNED sang IN_PROGRESS
     */
    @Transactional
    public AppointmentResponse startAppointment(UUID appointmentId) {
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Kiểm tra appointment phải ở trạng thái ASSIGNED
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.ASSIGNED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Chuyển sang IN_PROGRESS
        appointment.setStatus(ServiceAppointment.AppointmentStatus.IN_PROGRESS);
        appointment.setUpdatedAt(LocalDateTime.now());

        ServiceAppointment startedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(startedAppointment);
    }

    private AppointmentResponse convertToResponse(ServiceAppointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        
        // Handle customer with null safety
        if (appointment.getCustomer() != null) {
            try {
                response.setCustomerId(appointment.getCustomer().getId());
                response.setCustomerName(appointment.getCustomer().getFullName());
                response.setCustomerPhone(appointment.getCustomer().getPhone());
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setCustomerId(null);
                response.setCustomerName("Unknown");
                response.setCustomerPhone(null);
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
        
        // Map selected packages JSON
        response.setSelectedPackages(appointment.getSelectedPackages());
        
        // Parse selected packages and get names
        if (appointment.getSelectedPackages() != null && !appointment.getSelectedPackages().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> packageIdStrings = mapper.readValue(
                    appointment.getSelectedPackages(), 
                    new TypeReference<List<String>>() {}
                );
                
                // Convert strings to UUIDs and fetch package names
                List<String> packageNames = packageIdStrings.stream()
                    .map(idStr -> {
                        try {
                            UUID packageId = UUID.fromString(idStr);
                            return servicePackageRepository.findById(packageId)
                                .map(ServicePackage::getName)
                                .orElse("Unknown Package");
                        } catch (Exception e) {
                            return "Invalid Package";
                        }
                    })
                    .collect(Collectors.toList());
                
                response.setSelectedPackageNames(String.join(", ", packageNames));
            } catch (Exception e) {
                // Fallback to single package name
                response.setSelectedPackageNames(appointment.getServicePackage() != null ? 
                    appointment.getServicePackage().getName() : null);
            }
        } else {
            // No selected packages, use service package name
            response.setSelectedPackageNames(appointment.getServicePackage() != null ? 
                appointment.getServicePackage().getName() : null);
        }
        
        // Map technician information
        if (appointment.getTechnician() != null) {
            try {
                response.setTechnicianId(appointment.getTechnician().getId());
                if (appointment.getTechnician().getUser() != null) {
                    response.setTechnicianName(appointment.getTechnician().getUser().getFullName());
                }
            } catch (Exception e) {
                // Handle lazy loading exception
                response.setTechnicianId(null);
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
    
    /**
     * Tự động tạo invoice khi appointment được hoàn thành
     */
    private void createInvoiceForCompletedAppointment(ServiceAppointment appointment) {
        // 1. Kiểm tra xem đã có service order chưa
        Optional<ServiceOrder> serviceOrderOpt = serviceOrderRepository.findByAppointmentId(appointment.getId());
        
        if (serviceOrderOpt.isEmpty()) {
            return;
        }
        
        ServiceOrder serviceOrder = serviceOrderOpt.get();
        
        // 2. Kiểm tra xem đã có invoice chưa (tránh tạo trùng)
        if (invoiceService.getInvoiceByServiceOrderId(serviceOrder.getId()) != null) {
            return;
        }
        
        // 3. Tính toán chi phí từ service package
        BigDecimal subtotal = BigDecimal.ZERO;
        if (appointment.getServicePackage() != null && appointment.getServicePackage().getPrice() != null) {
            subtotal = appointment.getServicePackage().getPrice();
        }
        
        // 4. Tính thuế (10%)
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.10"));
        
        // 5. Tạo invoice request
        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
        invoiceRequest.setServiceOrderId(serviceOrder.getId());
        invoiceRequest.setSubtotal(subtotal);
        invoiceRequest.setTaxAmount(taxAmount);
        invoiceRequest.setDiscountAmount(BigDecimal.ZERO);
        invoiceRequest.setDueDate(LocalDateTime.now().plusDays(7)); // Hạn thanh toán 7 ngày
        
        // 6. Tạo invoice
        invoiceService.createInvoice(invoiceRequest);
    }

    /**
     * Lấy danh sách gói dịch vụ của appointment (hỗ trợ multiple packages)
     */
    @Transactional(readOnly = true)
    public List<com.swp391.EV.service.dto.AppointmentPackageDTO> getAppointmentPackages(UUID appointmentId) {
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        
        // Try to parse selected_packages JSON field first
        String selectedPackagesJson = appointment.getSelectedPackages();
        if (selectedPackagesJson != null && !selectedPackagesJson.isEmpty() && !selectedPackagesJson.equals("[]")) {
            try {
                // Parse JSON array: [{"packageId": "uuid", "packageName": "...", "price": 1200000}, ...]
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, Object>> packages = 
                    mapper.readValue(selectedPackagesJson, new com.fasterxml.jackson.core.type.TypeReference<>() {});
                
                return packages.stream()
                        .map(pkg -> com.swp391.EV.service.dto.AppointmentPackageDTO.builder()
                                .packageId(UUID.fromString(pkg.get("packageId").toString()))
                                .packageName(pkg.get("packageName").toString())
                                .description(pkg.getOrDefault("description", "").toString())
                                .price(new java.math.BigDecimal(pkg.get("price").toString()))
                                .durationMinutes(pkg.containsKey("durationMinutes") 
                                    ? Integer.valueOf(pkg.get("durationMinutes").toString()) 
                                    : null)
                                .build())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        // FALLBACK: Return single package from service_package_id (for backward compatibility)
        if (appointment.getServicePackage() == null) {
            return List.of(); // Không có gói nào
        }
        
        // Force initialize the lazy proxy within transaction
        ServicePackage pkg = appointment.getServicePackage();
        // Access properties to trigger initialization
        pkg.getName();
        
        return List.of(
                com.swp391.EV.service.dto.AppointmentPackageDTO.builder()
                        .packageId(pkg.getId())
                        .packageName(pkg.getName())
                        .description(pkg.getDescription())
                        .price(pkg.getPrice())
                        .durationMinutes(pkg.getDurationMinutes())
                        .build()
        );
    }
}

