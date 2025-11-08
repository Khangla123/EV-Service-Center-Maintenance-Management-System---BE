package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateServiceOrderRequest;
import com.swp391.EV.service.dto.request.UpdateServiceOrderRequest;
import com.swp391.EV.service.dto.response.ServiceOrderResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.*;
import com.swp391.EV.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceAppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;

    public List<ServiceOrderResponse> getAllServiceOrders() {
        return serviceOrderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * DÀNH CHO STAFF - Tạo đơn dịch vụ trực tiếp từ appointment
     * Staff có thể tạo service order trực tiếp mà không cần qua flow confirm của customer
     */
    @Transactional
    public ServiceOrderResponse createServiceOrder(CreateServiceOrderRequest request) {
        ServiceAppointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Technician co the null khi tao moi (chua phan cong)
        UUID technicianUserId = null;
        if (request.getTechnicianId() != null) {
            // Tìm staff theo ID để kiểm tra và lấy user.id
            Staff technicianStaff = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            technicianUserId = technicianStaff.getUser().getId();
        }

        // Generate order code
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technicianUserId(technicianUserId) // Lưu user.id vào DB
                // NOTE: Status removed - managed in appointment.status
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);
        return convertToResponse(savedOrder);
    }

    public ServiceOrderResponse getServiceOrderById(UUID id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        return convertToResponse(serviceOrder);
    }

    @Transactional
    public ServiceOrderResponse updateServiceOrder(UUID id, UpdateServiceOrderRequest request) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        // NOTE: Status removed - use AppointmentService to update appointment.status
        // if (request.getStatus() != null) {
        //     serviceOrder.setStatus(request.getStatus());
        // }
        if (request.getTechnicianId() != null) {
            Staff technician = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            serviceOrder.setTechnicianUserId(technician.getUser().getId()); // Lưu user.id vào DB
        }
        if (request.getStartTime() != null) {
            serviceOrder.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            serviceOrder.setEndTime(request.getEndTime());
        }
        if (request.getChecklist() != null) {
            serviceOrder.setChecklist(request.getChecklist());
        }
        if (request.getDiagnosis() != null) {
            serviceOrder.setDiagnosis(request.getDiagnosis());
        }
        if (request.getWorkPerformed() != null) {
            serviceOrder.setWorkPerformed(request.getWorkPerformed());
        }
        if (request.getTotalAmount() != null) {
            serviceOrder.setTotalAmount(request.getTotalAmount());
        }

        serviceOrder.setUpdatedAt(LocalDateTime.now());
        ServiceOrder updatedOrder = serviceOrderRepository.save(serviceOrder);
        return convertToResponse(updatedOrder);
    }

    /**
     * Phân công lại technician cho service order đã tồn tại
     * Chỉ dùng để thay đổi technician, KHÔNG tạo service order mới
     * @param orderId ID của service order
     * @param technicianId Có thể là USER_ID hoặc STAFF_ID - tự động detect
     */
    @Transactional
    public ServiceOrderResponse assignTechnician(UUID orderId, UUID technicianId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        // Thử tìm theo Staff.id trước
        Optional<Staff> staffOpt = staffRepository.findById(technicianId);
        
        // Nếu không tìm thấy, thử tìm theo User.id
        if (staffOpt.isEmpty()) {
            staffOpt = staffRepository.findByUserId(technicianId);
        }
        
        Staff staff = staffOpt.orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        serviceOrder.setTechnician(staff); // Set Staff entity trực tiếp
        serviceOrder.setUpdatedAt(LocalDateTime.now());

        ServiceOrder updatedOrder = serviceOrderRepository.save(serviceOrder);
        return convertToResponse(updatedOrder);
    }

    // NOTE: Status management removed from ServiceOrder
    // Status is now managed in ServiceAppointment.status
    // Use AppointmentService.updateStatus() instead
    /*
    @Transactional
    public ServiceOrderResponse updateStatus(UUID orderId, ServiceOrder.ServiceStatus status) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        serviceOrder.setStatus(status);
        serviceOrder.setUpdatedAt(LocalDateTime.now());

        if (status == ServiceOrder.ServiceStatus.IN_PROGRESS && serviceOrder.getStartTime() == null) {
            serviceOrder.setStartTime(LocalDateTime.now());
        }
        if (status == ServiceOrder.ServiceStatus.COMPLETED && serviceOrder.getEndTime() == null) {
            serviceOrder.setEndTime(LocalDateTime.now());
        }

        ServiceOrder updatedOrder = serviceOrderRepository.save(serviceOrder);
        return convertToResponse(updatedOrder);
    }
    */

    public List<ServiceOrderResponse> getTechnicianTasks(UUID technicianId) {
        // technicianId ở đây là staffId (Staff.id)
        return serviceOrderRepository.findByTechnicianId(technicianId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo Service Order từ Appointment và phân công Technician ngay
     * Đây là flow chính: Appointment (CONFIRMED) -> Phân công thợ -> Tạo Service Order
     * @param appointmentId ID của appointment đã CONFIRMED
     * @param technicianId QUAN TRỌNG: Đây là USER_ID (từ bảng users), KHÔNG phải staff.id
     */
    @Transactional
    public ServiceOrderResponse createServiceOrderFromAppointment(UUID appointmentId, UUID technicianId) {
        // 1. Kiểm tra Appointment có tồn tại và đã CONFIRMED chưa
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        System.out.println("=== VALIDATION CHECK ===");
        System.out.println("Appointment ID: " + appointmentId);
        System.out.println("Appointment Status: " + appointment.getStatus());
        System.out.println("Required Status: CONFIRMED");

        // 2. Kiểm tra appointment đã được xác nhận chưa
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.CONFIRMED) {
            System.out.println("❌ ERROR: Appointment status is not CONFIRMED!");
            System.out.println("   Current status: " + appointment.getStatus());
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Kiểm tra appointment đã có service order chưa (tránh tạo trùng)
        boolean hasExistingOrder = serviceOrderRepository.findByAppointmentId(appointmentId).isPresent();
        System.out.println("Has existing service order: " + hasExistingOrder);
        
        if (hasExistingOrder) {
            System.out.println("❌ ERROR: Appointment already has a service order!");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        System.out.println("✅ Validation passed!");
        System.out.println("========================");

        // 4. Lấy thông tin kỹ thuật viên
        // CRITICAL: technicianId là USER_ID từ frontend
        Staff technicianStaff = staffRepository.findByUserId(technicianId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        
        UUID technicianUserId = technicianStaff.getUser().getId();

        // Log để debug
        System.out.println("=== DEBUG CREATE SERVICE ORDER ===");
        System.out.println("Input technicianId (user.id from FE): " + technicianId);
        System.out.println("Found Staff.id: " + technicianStaff.getId());
        System.out.println("Found Staff.user.id: " + technicianUserId);
        System.out.println("Will save technician_id to DB: " + technicianUserId + " (user.id) - CORRECT!");
        System.out.println("===================================");

        // 5. Load checklist template từ maintenance_plans
        String checklistJson = null;
        try {
            UUID servicePackageId = appointment.getServicePackage().getId();
            UUID vehicleModelId = appointment.getVehicle().getVehicleModel().getId();
            
            Optional<MaintenancePlan> maintenancePlan = maintenancePlanRepository
                    .findByServicePackageIdAndVehicleModelId(servicePackageId, vehicleModelId);
            
            if (maintenancePlan.isEmpty()) {
                // Fallback: Tìm plan chung không phân biệt vehicle model
                maintenancePlan = maintenancePlanRepository.findByServicePackageId(servicePackageId);
            }
            
            if (maintenancePlan.isPresent()) {
                checklistJson = maintenancePlan.get().getChecklistTemplate();
                System.out.println("=== LOADED CHECKLIST TEMPLATE ===");
                System.out.println("From maintenance_plan_id: " + maintenancePlan.get().getId());
                System.out.println("Checklist JSON: " + (checklistJson != null ? checklistJson.substring(0, Math.min(100, checklistJson.length())) : "null"));
                System.out.println("==================================");
            } else {
                System.out.println("⚠️ WARNING: No maintenance plan found for service package: " + servicePackageId);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR loading checklist template: " + e.getMessage());
            e.printStackTrace();
        }

        // 5. Tạo Service Order mới với technician đã được phân công
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technician(technicianStaff) // Set Staff entity trực tiếp
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        System.out.println("=== BEFORE SAVE SERVICE ORDER ===");
        System.out.println("Service Order to be saved:");
        System.out.println("  - orderCode: " + serviceOrder.getOrderCode());
        System.out.println("  - appointment.id: " + serviceOrder.getAppointment().getId());
        System.out.println("  - technician.id (staff.id): " + serviceOrder.getTechnician().getId());
        System.out.println("  - startTime: " + serviceOrder.getStartTime());
        System.out.println("  - endTime: " + serviceOrder.getEndTime());
        System.out.println("==================================");

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        System.out.println("=== AFTER SAVE SERVICE ORDER ===");
        System.out.println("Saved Service Order:");
        System.out.println("  - id (generated): " + savedOrder.getId());
        System.out.println("  - orderCode: " + savedOrder.getOrderCode());
        System.out.println("  - appointment_id (FK): " + savedOrder.getAppointment().getId());
        System.out.println("  - technician_id (FK to staff.id): " + savedOrder.getTechnician().getId());
        System.out.println("=================================");

        // 6. Cập nhật trạng thái appointment thành ASSIGNED và gán technician (Staff)
        appointment.setStatus(ServiceAppointment.AppointmentStatus.ASSIGNED);
        appointment.setTechnician(technicianStaff); // Gán Staff vào appointment để hiển thị tên
        appointmentRepository.save(appointment);

        System.out.println("=== APPOINTMENT UPDATED ===");
        System.out.println("Appointment status changed to: " + appointment.getStatus());
        System.out.println("Appointment technician set to: " + technicianStaff.getUser().getFullName());
        System.out.println("===========================");

        return convertToResponse(savedOrder);
    }

    /**
     * Get service order by appointment ID
     */
    public ServiceOrderResponse getServiceOrderByAppointmentId(UUID appointmentId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        return convertToResponse(serviceOrder);
    }

    private ServiceOrderResponse convertToResponse(ServiceOrder serviceOrder) {
        ServiceOrderResponse response = new ServiceOrderResponse();
        response.setId(serviceOrder.getId());
        response.setAppointmentId(serviceOrder.getAppointment().getId());
        response.setOrderCode(serviceOrder.getOrderCode());
        
        // Lấy thông tin technician từ technicianUserId
        if (serviceOrder.getTechnicianUserId() != null) {
            response.setTechnicianId(serviceOrder.getTechnicianUserId());
            // Nếu có quan hệ technician được lazy load
            if (serviceOrder.getTechnician() != null) {
                response.setTechnicianName(serviceOrder.getTechnician().getUser().getFullName());
            }
        }
        
        // NOTE: Status được lấy từ appointment.status, không lưu ở service_order nữa
        // response.setStatus() - REMOVED
        response.setStartTime(serviceOrder.getStartTime());
        response.setEndTime(serviceOrder.getEndTime());
        response.setChecklist(serviceOrder.getChecklist());
        response.setDiagnosis(serviceOrder.getDiagnosis());
        response.setWorkPerformed(serviceOrder.getWorkPerformed());
        response.setTotalAmount(serviceOrder.getTotalAmount());
        response.setCreatedAt(serviceOrder.getCreatedAt());
        response.setUpdatedAt(serviceOrder.getUpdatedAt());
        return response;
    }
}
