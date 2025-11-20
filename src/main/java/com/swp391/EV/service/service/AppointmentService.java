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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                .selectedPackages(request.getSelectedPackages())
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
     * Staff hủy lịch hẹn với lý do
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

    /**
     * Mục đích chính: Chuyển đổi entity ServiceAppointment (dữ liệu từ database)
     * sang AppointmentResponse DTO (dữ liệu gửi cho client)Chuyển đổi ServiceAppointment entity
     * sang AppointmentResponse DTO
     * Phương thức này xử lý việc map dữ liệu từ các entity liên quan sang response object
     * để trả về cho client, đảm bảo không bị lỗi khi các relationship null
     */
    private AppointmentResponse convertToResponse(ServiceAppointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        // Set ID của appointment
        response.setId(appointment.getId());
        
        // === XỬ LÝ THÔNG TIN KHÁCH HÀNG ===
        // Kiểm tra và set thông tin customer (có thể null)
        if (appointment.getCustomer() != null) {
            try {
                response.setCustomerId(appointment.getCustomer().getId());
                response.setCustomerName(appointment.getCustomer().getFullName());
                response.setCustomerPhone(appointment.getCustomer().getPhone());
            } catch (Exception e) {
                // Nếu có lỗi khi lấy thông tin customer (lazy loading issue), set giá trị mặc định
                response.setCustomerId(null);
                response.setCustomerName("Unknown");
                response.setCustomerPhone(null);
            }
        }
        
        // === XỬ LÝ THÔNG TIN XE ===
        // Kiểm tra và set thông tin vehicle
        if (appointment.getVehicle() != null) {
            try {
                UUID vehicleId = appointment.getVehicle().getId();
                response.setVehicleId(vehicleId);
                response.setVehicleLicensePlate(appointment.getVehicle().getLicensePlate());

                // Lấy thêm thông tin model của xe (nested relationship)
                if (appointment.getVehicle().getVehicleModel() != null) {
                    response.setVehicleModel(appointment.getVehicle().getVehicleModel().getModel());
                }
            } catch (Exception e) {
                // Nếu lỗi khi lấy thông tin xe, set null
                response.setVehicleId(null);
            }
        }
        
        // === XỬ LÝ THÔNG TIN TRUNG TÂM DỊCH VỤ ===
        if (appointment.getServiceCenter() != null) {
            try {
                response.setServiceCenterId(appointment.getServiceCenter().getId());
                response.setServiceCenterName(appointment.getServiceCenter().getName());
            } catch (Exception e) {
                response.setServiceCenterId(null);
            }
        }
        
        // === XỬ LÝ THÔNG TIN GÓI DỊCH VỤ CHÍNH ===
        if (appointment.getServicePackage() != null) {
            try {
                response.setServicePackageId(appointment.getServicePackage().getId());
                response.setServicePackageName(appointment.getServicePackage().getName());
            } catch (Exception e) {
                response.setServicePackageId(null);
            }
        }
        
        // === XỬ LÝ CỘT SELECTED_PACKAGES (JSON STRING) ===
        // Cột này lưu danh sách UUID của các gói dịch vụ được chọn dưới dạng JSON string
        // Ví dụ: ["uuid1", "uuid2", "uuid3"]
        response.setSelectedPackages(appointment.getSelectedPackages());
        
        // Parse JSON string để lấy tên các gói dịch vụ
        if (appointment.getSelectedPackages() != null && !appointment.getSelectedPackages().trim().isEmpty()) {
            try {
                // Sử dụng Jackson ObjectMapper để parse JSON string
                ObjectMapper mapper = new ObjectMapper();
                // Parse JSON string thành List<String> chứa các UUID
                List<String> packageIdStrings = mapper.readValue(
                    appointment.getSelectedPackages(), 
                    new TypeReference<List<String>>() {}
                );
                
                // Với mỗi UUID string, query database để lấy tên gói dịch vụ
                List<String> packageNames = packageIdStrings.stream()
                    .map(idStr -> {
                        try {
                            // Chuyển string UUID thành UUID object
                            UUID packageId = UUID.fromString(idStr);
                            // Tìm service package trong database và lấy tên
                            return servicePackageRepository.findById(packageId)
                                .map(ServicePackage::getName)
                                .orElse("Unknown Package"); // Nếu không tìm thấy
                        } catch (Exception e) {
                            // Nếu UUID không hợp lệ
                            return "Invalid Package";
                        }
                    })
                    .collect(Collectors.toList());
                
                // Join tất cả tên gói dịch vụ thành một chuỗi, phân cách bằng dấu phẩy
                // Ví dụ: "Gói A, Gói B, Gói C"
                response.setSelectedPackageNames(String.join(", ", packageNames));
            } catch (Exception e) {
                // Nếu parse JSON thất bại, fallback về gói dịch vụ chính
                response.setSelectedPackageNames(appointment.getServicePackage() != null ?
                    appointment.getServicePackage().getName() : null);
            }
        } else {
            // Nếu không có selected_packages (null hoặc rỗng),
            // sử dụng tên của gói dịch vụ chính làm mặc định
            response.setSelectedPackageNames(appointment.getServicePackage() != null ?
                appointment.getServicePackage().getName() : null);
        }
        
        // === XỬ LÝ THÔNG TIN KỸ THUẬT VIÊN ===
        // Technician được gán để thực hiện công việc
        if (appointment.getTechnician() != null) {
            try {
                response.setTechnicianId(appointment.getTechnician().getId());
                // Lấy tên từ User entity liên kết với Staff (nested relationship)
                if (appointment.getTechnician().getUser() != null) {
                    response.setTechnicianName(appointment.getTechnician().getUser().getFullName());
                }
            } catch (Exception e) {
                response.setTechnicianId(null);
            }
        }
        
        // === SET CÁC TRƯỜNG THÔNG TIN CƠ BẢN ===
        response.setAppointmentDate(appointment.getAppointmentDate());  // Ngày hẹn
        response.setStatus(appointment.getStatus());  // Trạng thái: PENDING, CONFIRMED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
        response.setNotes(appointment.getNotes());  // Ghi chú
        response.setEstimatedCompletion(appointment.getEstimatedCompletion());  // Thời gian dự kiến hoàn thành
        response.setActualCompletion(appointment.getActualCompletion());  // Thời gian thực tế hoàn thành
        response.setCreatedAt(appointment.getCreatedAt());  // Thời gian tạo
        response.setUpdatedAt(appointment.getUpdatedAt());  // Thời gian cập nhật cuối

        return response;
    }
    


    /**
     * Lấy danh sách gói dịch vụ của appointment (hỗ trợ multiple packages)
     */
    @Transactional(readOnly = true)
    public List<com.swp391.EV.service.dto.AppointmentPackageDTO> getAppointmentPackages(UUID appointmentId) {
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        
        String selectedPackagesJson = appointment.getSelectedPackages();
        if (selectedPackagesJson != null && !selectedPackagesJson.isEmpty() && !selectedPackagesJson.equals("[]")) {
            try {
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
            }
        }
        
        if (appointment.getServicePackage() == null) {
            return List.of();
        }
        
        ServicePackage pkg = appointment.getServicePackage();
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
