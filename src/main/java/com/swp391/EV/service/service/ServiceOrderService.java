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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceAppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;

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
        Staff technicianStaff = null;
        if (request.getTechnicianId() != null) {
            technicianStaff = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        }

        // Generate order code
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technician(technicianStaff) // Lưu Staff (phù hợp với DB FK: staff.id)
                .status(ServiceOrder.ServiceStatus.WAITING)
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

        if (request.getStatus() != null) {
            serviceOrder.setStatus(request.getStatus());
        }
        if (request.getTechnicianId() != null) {
            Staff technician = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            serviceOrder.setTechnician(technician); // Lưu Staff (phù hợp DB FK: staff.id)
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
     */
    @Transactional
    public ServiceOrderResponse assignTechnician(UUID orderId, UUID technicianId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        // Lấy thông tin kỹ thuật viên từ bảng staff
        Staff staff = staffRepository.findById(technicianId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        serviceOrder.setTechnician(staff); // Lưu Staff (phù hợp DB FK: staff.id)
        serviceOrder.setUpdatedAt(LocalDateTime.now());

        ServiceOrder updatedOrder = serviceOrderRepository.save(serviceOrder);
        return convertToResponse(updatedOrder);
    }

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

    public List<ServiceOrderResponse> getTechnicianTasks(UUID technicianId) {
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

        // 2. Kiểm tra appointment đã được xác nhận chưa
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Kiểm tra appointment đã có service order chưa (tránh tạo trùng)
        if (serviceOrderRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 4. Lấy thông tin kỹ thuật viên
        // CRITICAL: technicianId là USER_ID từ frontend, phải tìm staff theo user_id
        Staff technician = staffRepository.findByUserId(technicianId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Log để debug
        System.out.println("=== DEBUG CREATE SERVICE ORDER ===");
        System.out.println("Input technicianId (user.id from FE): " + technicianId);
        System.out.println("Found Staff.id: " + technician.getId());
        System.out.println("Found Staff.user.id: " + technician.getUser().getId());
        System.out.println("Will save technician_id to DB: " + technician.getId() + " (staff.id)");
        System.out.println("===================================");

        // 5. Tạo Service Order mới với technician đã được phân công
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technician(technician) // Lưu Staff (phù hợp DB FK: staff.id)
                .status(ServiceOrder.ServiceStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        // 6. Cập nhật trạng thái appointment thành ASSIGNED và gán technician
        appointment.setStatus(ServiceAppointment.AppointmentStatus.ASSIGNED);
        appointment.setTechnician(technician); // Gán technician vào appointment để hiển thị tên
        appointmentRepository.save(appointment);

        return convertToResponse(savedOrder);
    }

    private ServiceOrderResponse convertToResponse(ServiceOrder serviceOrder) {
        ServiceOrderResponse response = new ServiceOrderResponse();
        response.setId(serviceOrder.getId());
        response.setAppointmentId(serviceOrder.getAppointment().getId());
        response.setOrderCode(serviceOrder.getOrderCode());
        if (serviceOrder.getTechnician() != null) {
            response.setTechnicianId(serviceOrder.getTechnician().getId()); // staff.id
            // technician là Staff, lấy tên từ User
            response.setTechnicianName(serviceOrder.getTechnician().getUser().getFullName());
        }
        response.setStatus(serviceOrder.getStatus());
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
