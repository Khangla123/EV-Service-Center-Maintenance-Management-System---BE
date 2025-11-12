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

import java.math.BigDecimal;
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
    private final ServiceOrderPartRepository serviceOrderPartRepository;
    private final PartRepository partRepository;
    private final ServiceSuggestionRepository serviceSuggestionRepository;

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
        System.out.println("===========================");
        System.out.println("🚀 CREATE SERVICE ORDER FROM APPOINTMENT - START");
        System.out.println("Input appointmentId: " + appointmentId);
        System.out.println("Input technicianId: " + technicianId);
        System.out.println("===========================");
        
        // 1. Kiểm tra Appointment có tồn tại và đã CONFIRMED chưa
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        System.out.println("=== VALIDATION CHECK ===");
        System.out.println("Appointment ID: " + appointmentId);
        System.out.println("Appointment Status: " + appointment.getStatus());
        System.out.println("Appointment Customer: " + appointment.getCustomer().getUsername());
        System.out.println("Appointment Service Package: " + appointment.getServicePackage().getName());
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
                .checklist(checklistJson) // Set checklist template từ maintenance_plans
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

    public ServiceOrderResponse updateIssues(UUID serviceOrderId, String issuesJson) {
        System.out.println("===========================");
        System.out.println("🔍 UPDATE ISSUES - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        System.out.println("Issues JSON received: " + issuesJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        System.out.println("📝 Old issues: " + serviceOrder.getIssues());
        
        serviceOrder.setIssues(issuesJson);
        serviceOrder.setUpdatedAt(LocalDateTime.now());
        
        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);
        
        System.out.println("💾 New issues saved: " + savedOrder.getIssues());
        System.out.println("✅ UPDATE ISSUES - COMPLETED");
        System.out.println("===========================");
        
        return convertToResponse(savedOrder);
    }

    /**
     * Add parts used to service order
     * @param serviceOrderId Service order ID
     * @param partsJson JSON array: [{"partCode": "PT001", "partName": "...", "quantity": 2, "unit": "cái"}]
     */
    @Transactional
    public ServiceOrderResponse addPartsUsed(UUID serviceOrderId, String partsJson) {
        System.out.println("===========================");
        System.out.println("🔧 ADD PARTS USED - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        System.out.println("Parts JSON received: " + partsJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        
        try {
            // Parse JSON to list of part info
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> partsList = mapper.readValue(
                partsJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );
            
            System.out.println("📦 Parsed " + partsList.size() + " parts");
            
            for (java.util.Map<String, Object> partInfo : partsList) {
                String partCode = (String) partInfo.get("partCode");
                Integer quantity = (Integer) partInfo.get("quantity");
                
                System.out.println("🔍 Processing part: " + partCode + " x " + quantity);
                
                // Find part by code
                Part part = partRepository.findByPartCode(partCode)
                        .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
                
                System.out.println("✅ Found part: " + part.getName() + " (ID: " + part.getId() + ")");
                System.out.println("💰 Unit price: " + part.getUnitPrice());
                
                // Create service_order_part record
                ServiceOrderPart orderPart = ServiceOrderPart.builder()
                        .serviceOrder(serviceOrder)
                        .part(part)
                        .quantity(quantity)
                        .unitPrice(part.getUnitPrice())
                        .createdAt(LocalDateTime.now())
                        .build();
                
                serviceOrderPartRepository.save(orderPart);
                System.out.println("✅ Saved service_order_part record");
                
                // Update part stock quantity
                int newStock = part.getStockQuantity() - quantity;
                part.setStockQuantity(newStock);
                partRepository.save(part);
                System.out.println("📦 Updated stock: " + (part.getStockQuantity() + quantity) + " -> " + newStock);
            }
            
            System.out.println("✅ ADD PARTS USED - COMPLETED");
            System.out.println("===========================");
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing/saving parts: " + e.getMessage());
            e.printStackTrace();
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return convertToResponse(serviceOrder);
    }

    /**
     * Get parts used for a service order (Simple version - return count only)
     * @param serviceOrderId Service order ID
     * @return Count of parts used
     */
    public int getPartsUsedCount(UUID serviceOrderId) {
        System.out.println("===========================");
        System.out.println("🔧 GET PARTS USED COUNT - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        int count = parts.size();
        
        System.out.println("📦 Found " + count + " parts");
        System.out.println("✅ GET PARTS USED COUNT - COMPLETED");
        System.out.println("===========================");
        
        return count;
    }

    /**
     * Get parts summary (count + total price)
     * @param serviceOrderId Service order ID
     * @return Map with "count" and "total"
     */
    public java.util.Map<String, Object> getPartsUsedSummary(UUID serviceOrderId) {
        System.out.println("===========================");
        System.out.println("💰 GET PARTS SUMMARY - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        int count = parts.size();
        
        BigDecimal total = parts.stream()
                .map(ServiceOrderPart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        System.out.println("📦 Found " + count + " parts, Total: " + total);
        System.out.println("✅ GET PARTS SUMMARY - COMPLETED");
        System.out.println("===========================");
        
        return java.util.Map.of("count", count, "total", total);
    }

    /**
     * Get detailed list of parts used
     * @param serviceOrderId Service order ID
     * @return List of ServiceOrderPartResponse
     */
    public List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse> getPartsUsed(UUID serviceOrderId) {
        System.out.println("===========================");
        System.out.println("🔧 GET PARTS LIST - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        System.out.println("📦 Found " + parts.size() + " parts");
        
        List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse> response = new java.util.ArrayList<>();
        for (ServiceOrderPart sop : parts) {
            Part partEntity = sop.getPart();
            com.swp391.EV.service.dto.response.ServiceOrderPartResponse dto = 
                com.swp391.EV.service.dto.response.ServiceOrderPartResponse.builder()
                    .id(sop.getId())
                    .partCode(partEntity != null ? partEntity.getPartCode() : null)
                    .partName(partEntity != null ? partEntity.getName() : null)
                    .quantity(sop.getQuantity())
                    .unitPrice(sop.getUnitPrice())
                    .totalPrice(sop.getTotalPrice())
                    .build();
            response.add(dto);
        }
        
        System.out.println("✅ GET PARTS LIST - COMPLETED");
        System.out.println("===========================");
        
        return response;
    }

    /**
     * Add service suggestion (recommended additional service)
     * @param serviceOrderId Service order ID
     * @param suggestionJson JSON: {"serviceName": "...", "reason": "...", "estimatedCost": 500000}
     */
    @Transactional
    public ServiceOrderResponse addServiceSuggestion(UUID serviceOrderId, String suggestionJson) {
        System.out.println("===========================");
        System.out.println("💡 ADD SERVICE SUGGESTION - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        System.out.println("Suggestion JSON received: " + suggestionJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        System.out.println("✅ Service Order found: " + serviceOrder.getOrderCode());
        
        try {
            // Parse JSON to suggestion info
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> suggestionData = mapper.readValue(
                suggestionJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
            );
            
            String serviceName = (String) suggestionData.get("serviceName");
            String reason = (String) suggestionData.get("reason");
            Object costObj = suggestionData.get("estimatedCost");
            
            // Handle estimatedCost - could be String or Number
            BigDecimal estimatedCost = BigDecimal.ZERO;
            if (costObj != null) {
                if (costObj instanceof Number) {
                    estimatedCost = BigDecimal.valueOf(((Number) costObj).doubleValue());
                } else if (costObj instanceof String) {
                    // Remove any non-numeric characters except dot
                    String costStr = ((String) costObj).replaceAll("[^0-9.]", "");
                    estimatedCost = new BigDecimal(costStr);
                }
            }
            
            System.out.println("📝 Service: " + serviceName);
            System.out.println("📝 Reason: " + reason);
            System.out.println("💰 Estimated cost: " + estimatedCost);
            
            // Create service suggestion record
            ServiceSuggestion suggestion = ServiceSuggestion.builder()
                    .serviceOrder(serviceOrder)
                    .serviceName(serviceName)
                    .reason(reason)
                    .estimatedCost(estimatedCost)
                    .status(ServiceSuggestion.SuggestionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            serviceSuggestionRepository.save(suggestion);
            System.out.println("✅ Saved service suggestion");
            
            System.out.println("✅ ADD SERVICE SUGGESTION - COMPLETED");
            System.out.println("===========================");
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing/saving suggestion: " + e.getMessage());
            e.printStackTrace();
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return convertToResponse(serviceOrder);
    }

    /**
     * Get service suggestions for a service order (return DTO to avoid lazy loading)
     */
    public List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse> getServiceSuggestions(UUID serviceOrderId) {
        System.out.println("===========================");
        System.out.println("📋 GET SERVICE SUGGESTIONS - START");
        System.out.println("Service Order ID: " + serviceOrderId);
        
        List<ServiceSuggestion> suggestions = serviceSuggestionRepository.findByServiceOrderId(serviceOrderId);
        
        System.out.println("📦 Found " + suggestions.size() + " suggestions");
        
        List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse> responses = suggestions.stream()
                .map(s -> com.swp391.EV.service.dto.response.ServiceSuggestionResponse.builder()
                        .id(s.getId())
                        .serviceName(s.getServiceName())
                        .reason(s.getReason())
                        .estimatedCost(s.getEstimatedCost())
                        .status(s.getStatus())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        
        System.out.println("✅ GET SERVICE SUGGESTIONS - COMPLETED");
        System.out.println("===========================");
        
        return responses;
    }

    /**
     * Update suggestion status (APPROVED/REJECTED)
     */
    @Transactional
    public ServiceSuggestion updateSuggestionStatus(UUID suggestionId, ServiceSuggestion.SuggestionStatus status) {
        ServiceSuggestion suggestion = serviceSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        suggestion.setStatus(status);
        suggestion.setUpdatedAt(LocalDateTime.now());
        
        return serviceSuggestionRepository.save(suggestion);
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
            try {
                if (serviceOrder.getTechnician() != null) {
                    response.setTechnicianName(serviceOrder.getTechnician().getUser().getFullName());
                }
            } catch (Exception e) {
                // LazyInitializationException - skip technician name
                response.setTechnicianName("Unknown");
            }
        }
        
        // NOTE: Status được lấy từ appointment.status, không lưu ở service_order nữa
        // response.setStatus() - REMOVED
        response.setStartTime(serviceOrder.getStartTime());
        response.setEndTime(serviceOrder.getEndTime());
        response.setChecklist(serviceOrder.getChecklist());
        response.setIssues(serviceOrder.getIssues());
        response.setDiagnosis(serviceOrder.getDiagnosis());
        response.setWorkPerformed(serviceOrder.getWorkPerformed());
        response.setTotalAmount(serviceOrder.getTotalAmount());
        response.setCreatedAt(serviceOrder.getCreatedAt());
        response.setUpdatedAt(serviceOrder.getUpdatedAt());
        return response;
    }
}
