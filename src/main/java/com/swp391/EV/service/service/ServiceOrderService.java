package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateServiceOrderRequest;
import com.swp391.EV.service.dto.request.UpdateServiceOrderRequest;
import com.swp391.EV.service.dto.response.ServiceOrderResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.*;
import com.swp391.EV.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ServiceOrderService {

    // Constants for log messages
    private static final String SERVICE_ORDER_FOUND = "Service Order found: {}";
    private static final String SERVICE_ORDER_ID_LOG = "Service Order ID: {}";

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceAppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final ServiceOrderPartRepository serviceOrderPartRepository;
    private final PartRepository partRepository;
    private final ServiceSuggestionRepository serviceSuggestionRepository;

    public List<ServiceOrderResponse> getAllServiceOrders() {
        // Use JOIN FETCH query to eagerly load customer and vehicle
        List<ServiceOrder> serviceOrders = serviceOrderRepository.findAllWithCustomerAndVehicle();
        
        return serviceOrders.stream()
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
        log.info("=== CREATE SERVICE ORDER FROM APPOINTMENT - START ===");
        log.info("Input appointmentId: {}", appointmentId);
        log.info("Input technicianId: {}", technicianId);
        
        // 1. Kiểm tra Appointment có tồn tại và đã CONFIRMED chưa
        ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        log.info("=== VALIDATION CHECK ===");
        log.info("Appointment ID: {}", appointmentId);
        log.info("Appointment Status: {}", appointment.getStatus());
        log.info("Appointment Customer: {}", appointment.getCustomer().getUsername());
        log.info("Appointment Service Package: {}", appointment.getServicePackage().getName());
        log.info("Required Status: CONFIRMED");

        // 2. Kiểm tra appointment đã được xác nhận chưa
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.CONFIRMED) {
            log.error("Appointment status is not CONFIRMED! Current status: {}", appointment.getStatus());
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Kiểm tra appointment đã có service order chưa (tránh tạo trùng)
        boolean hasExistingOrder = serviceOrderRepository.findByAppointmentId(appointmentId).isPresent();
        log.info("Has existing service order: {}", hasExistingOrder);
        
        if (hasExistingOrder) {
            log.error("Appointment already has a service order!");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        log.info("Validation passed!");

        // 4. Lấy thông tin kỹ thuật viên
        // CRITICAL: technicianId là USER_ID từ frontend
        Staff technicianStaff = staffRepository.findByUserId(technicianId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        
        UUID technicianUserId = technicianStaff.getUser().getId();

        // Log để debug
        log.debug("=== DEBUG CREATE SERVICE ORDER ===");
        log.debug("Input technicianId (user.id from FE): {}", technicianId);
        log.debug("Found Staff.id: {}", technicianStaff.getId());
        log.debug("Found Staff.user.id: {}", technicianUserId);
        log.debug("Will save technician_id to DB: {} (user.id)", technicianUserId);

        // 5. Load checklist templates from maintenance_plans for ALL selected packages
        String checklistJson = null;
        try {
            UUID vehicleModelId = appointment.getVehicle().getVehicleModel().getId();
            
            // Parse selected_packages JSON array to get all package IDs
            java.util.List<UUID> selectedPackageIds = new java.util.ArrayList<>();
            
            if (appointment.getSelectedPackages() != null && !appointment.getSelectedPackages().trim().isEmpty()) {
                log.info("=== PARSING SELECTED PACKAGES ===");
                log.info("Selected packages JSON: {}", appointment.getSelectedPackages());
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> packageIdStrings = mapper.readValue(
                    appointment.getSelectedPackages(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {}
                );
                
                for (String pkgIdStr : packageIdStrings) {
                    selectedPackageIds.add(UUID.fromString(pkgIdStr));
                }
                
                log.info("Parsed {} package IDs: {}", selectedPackageIds.size(), selectedPackageIds);
            } else {
                // Fallback: Use single service_package_id if selected_packages is empty
                UUID fallbackPackageId = appointment.getServicePackage().getId();
                selectedPackageIds.add(fallbackPackageId);
                log.warn("No selected_packages found, using fallback service_package_id: {}", fallbackPackageId);
            }
            
            // Load and merge checklist templates from all packages
            java.util.List<java.util.Map<String, Object>> mergedChecklist = new java.util.ArrayList<>();
            int orderCounter = 1;
            
            log.info("=== LOADING CHECKLIST TEMPLATES FOR {} PACKAGES ===", selectedPackageIds.size());
            
            for (UUID packageId : selectedPackageIds) {
                log.info("Loading checklist for package ID: {}", packageId);
                
                Optional<MaintenancePlan> maintenancePlan = maintenancePlanRepository
                        .findByServicePackageIdAndVehicleModelId(packageId, vehicleModelId);
                
                if (maintenancePlan.isEmpty()) {
                    // Fallback: Tìm plan chung không phân biệt vehicle model
                    maintenancePlan = maintenancePlanRepository.findByServicePackageId(packageId);
                }
                
                if (maintenancePlan.isPresent()) {
                    String templateJson = maintenancePlan.get().getChecklistTemplate();
                    log.info("Found maintenance_plan_id: {}", maintenancePlan.get().getId());
                    log.info("Checklist template preview: {}", templateJson != null ? templateJson.substring(0, Math.min(100, templateJson.length())) : "null");
                    
                    // Parse checklist JSON array
                    if (templateJson != null && !templateJson.trim().isEmpty()) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.List<java.util.Map<String, Object>> checklistItems = mapper.readValue(
                            templateJson,
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                        );
                        
                        // Re-number order field to avoid conflicts when merging
                        for (java.util.Map<String, Object> item : checklistItems) {
                            item.put("order", orderCounter++);
                        }
                        
                        mergedChecklist.addAll(checklistItems);
                        log.info("Added {} checklist items from package {}", checklistItems.size(), packageId);
                    }
                } else {
                    log.warn("No maintenance plan found for package ID: {}", packageId);
                }
            }
            
            // Convert merged checklist back to JSON string
            if (!mergedChecklist.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                checklistJson = mapper.writeValueAsString(mergedChecklist);
                log.info("=== MERGED CHECKLIST COMPLETE ===");
                log.info("Total checklist items: {}", mergedChecklist.size());
                log.info("Merged checklist JSON preview: {}", checklistJson.substring(0, Math.min(200, checklistJson.length())));
            } else {
                log.warn("No checklist items found for any selected packages");
            }
            
        } catch (Exception e) {
            log.error("ERROR loading/merging checklist templates: {}", e.getMessage(), e);
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

        log.debug("=== BEFORE SAVE SERVICE ORDER ===");
        log.debug("Service Order - orderCode: {}, appointmentId: {}, technicianId: {}", 
                  serviceOrder.getOrderCode(), 
                  serviceOrder.getAppointment().getId(), 
                  serviceOrder.getTechnician().getId());

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        log.info("=== SERVICE ORDER SAVED ===");
        log.info("Saved Service Order - id: {}, orderCode: {}, appointmentId: {}, technicianId: {}", 
                 savedOrder.getId(), 
                 savedOrder.getOrderCode(), 
                 savedOrder.getAppointment().getId(), 
                 savedOrder.getTechnician().getId());

        // 6. Cập nhật trạng thái appointment thành ASSIGNED và gán technician (Staff)
        appointment.setStatus(ServiceAppointment.AppointmentStatus.ASSIGNED);
        appointment.setTechnician(technicianStaff); // Gán Staff vào appointment để hiển thị tên
        appointmentRepository.save(appointment);

        log.info("=== APPOINTMENT UPDATED ===");
        log.info("Appointment status changed to: {}", appointment.getStatus());
        log.info("Appointment technician set to: {}", technicianStaff.getUser().getFullName());
        log.info("=== CREATE SERVICE ORDER COMPLETED ===");

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
        log.info("=== UPDATE ISSUES - START ===");
        log.info(SERVICE_ORDER_ID_LOG, serviceOrderId);
        log.debug("Issues JSON received: {}", issuesJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        log.info(SERVICE_ORDER_FOUND, serviceOrder.getOrderCode());
        log.debug("Old issues: {}", serviceOrder.getIssues());
        
        serviceOrder.setIssues(issuesJson);
        serviceOrder.setUpdatedAt(LocalDateTime.now());
        
        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);
        
        log.info("Issues updated successfully for Service Order: {}", savedOrder.getOrderCode());
        log.debug("New issues: {}", savedOrder.getIssues());
        
        return convertToResponse(savedOrder);
    }

    /**
     * Set price for a specific issue (Staff/Admin only)
     * @param serviceOrderId Service order ID
     * @param issueId Issue ID to update price
     * @param price Price to set
     * @return Updated service order
     */
    @Transactional
    public ServiceOrderResponse setIssuePrice(UUID serviceOrderId, String issueId, Double price) {
        log.info("=== SET ISSUE PRICE - START ===");
        log.info(SERVICE_ORDER_ID_LOG, serviceOrderId);
        log.info("Issue ID: {}, Price: {}", issueId, price);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        log.info(SERVICE_ORDER_FOUND, serviceOrder.getOrderCode());
        
        try {
            // Parse issues JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> issuesList = mapper.readValue(
                serviceOrder.getIssues() != null ? serviceOrder.getIssues() : "[]",
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );
            
            // Find and update the specific issue
            boolean found = false;
            for (java.util.Map<String, Object> issue : issuesList) {
                if (issueId.equals(issue.get("id"))) {
                    issue.put("price", price);
                    issue.put("staffSetPrice", true);
                    found = true;
                    log.info("Updated price for issue: {}", issueId);
                    break;
                }
            }
            
            if (!found) {
                log.error("Issue not found: {}", issueId);
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            // Save updated issues
            String updatedIssuesJson = mapper.writeValueAsString(issuesList);
            serviceOrder.setIssues(updatedIssuesJson);
            serviceOrder.setUpdatedAt(LocalDateTime.now());
            
            ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);
            log.info("=== SET ISSUE PRICE - COMPLETED ===");
            
            return convertToResponse(savedOrder);
            
        } catch (Exception e) {
            log.error("Error setting issue price: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * Add parts used to service order
     * @param serviceOrderId Service order ID
     * @param partsJson JSON array: [{"partCode": "PT001", "partName": "...", "quantity": 2, "unit": "cái"}]
     */
    @Transactional
    public ServiceOrderResponse addPartsUsed(UUID serviceOrderId, String partsJson) {
        log.info("=== ADD PARTS USED - START ===");
        log.info(SERVICE_ORDER_ID_LOG, serviceOrderId);
        log.debug("Parts JSON received: {}", partsJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        log.info(SERVICE_ORDER_FOUND, serviceOrder.getOrderCode());
        
        try {
            // Parse JSON to list of part info
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> partsList = mapper.readValue(
                partsJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );
            
            log.info("Parsed {} parts", partsList.size());
            
            for (java.util.Map<String, Object> partInfo : partsList) {
                String partCode = (String) partInfo.get("partCode");
                Integer quantity = (Integer) partInfo.get("quantity");
                
                log.debug("Processing part: {} x {}", partCode, quantity);
                
                // Find part by code
                Part part = partRepository.findByPartCode(partCode)
                        .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
                
                log.info("Found part: {} (ID: {}), Unit price: {}", part.getName(), part.getId(), part.getUnitPrice());
                
                // Create service_order_part record
                ServiceOrderPart orderPart = ServiceOrderPart.builder()
                        .serviceOrder(serviceOrder)
                        .part(part)
                        .quantity(quantity)
                        .unitPrice(part.getUnitPrice())
                        .createdAt(LocalDateTime.now())
                        .build();
                
                serviceOrderPartRepository.save(orderPart);
                log.debug("Saved service_order_part record");
                
                // Update part stock quantity
                int oldStock = part.getStockQuantity();
                int newStock = oldStock - quantity;
                part.setStockQuantity(newStock);
                partRepository.save(part);
                log.info("Updated stock for part {}: {} -> {}", partCode, oldStock, newStock);
            }
            
            log.info("=== ADD PARTS USED - COMPLETED ===");
            
        } catch (Exception e) {
            log.error("Error parsing/saving parts: {}", e.getMessage(), e);
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
        log.debug("Getting parts used count for Service Order ID: {}", serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        int count = parts.size();
        
        log.info("Found {} parts for Service Order: {}", count, serviceOrder.getOrderCode());
        
        return count;
    }

    /**
     * Get parts summary (count + total price)
     * @param serviceOrderId Service order ID
     * @return Map with "count" and "total"
     */
    public java.util.Map<String, Object> getPartsUsedSummary(UUID serviceOrderId) {
        log.debug("Getting parts summary for Service Order ID: {}", serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        log.debug(SERVICE_ORDER_FOUND, serviceOrder.getOrderCode());
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        int count = parts.size();
        
        BigDecimal total = parts.stream()
                .map(ServiceOrderPart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("Parts summary for Service Order {}: {} parts, Total: {}", serviceOrder.getOrderCode(), count, total);
        
        return java.util.Map.of("count", count, "total", total);
    }

    /**
     * Get detailed list of parts used
     * @param serviceOrderId Service order ID
     * @return List of ServiceOrderPartResponse
     */
    public List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse> getPartsUsed(UUID serviceOrderId) {
        log.debug("Getting parts list for Service Order ID: {}", serviceOrderId);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        log.info("Found {} parts for Service Order: {}", parts.size(), serviceOrder.getOrderCode());
        
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
        
        log.debug("Get parts list completed");
        
        return response;
    }

    /**
     * Add service suggestion (recommended additional service)
     * @param serviceOrderId Service order ID
     * @param suggestionJson JSON: {"serviceName": "...", "reason": "...", "estimatedCost": 500000}
     */
    @Transactional
    public ServiceOrderResponse addServiceSuggestion(UUID serviceOrderId, String suggestionJson) {
        log.info("=== ADD SERVICE SUGGESTION - START ===");
        log.info(SERVICE_ORDER_ID_LOG, serviceOrderId);
        log.debug("Suggestion JSON received: {}", suggestionJson);
        
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        log.info(SERVICE_ORDER_FOUND, serviceOrder.getOrderCode());
        
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
            
            log.info("Service: {}, Reason: {}, Estimated cost: {}", serviceName, reason, estimatedCost);
            
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
            log.info("Service suggestion saved successfully");
            log.info("=== ADD SERVICE SUGGESTION - COMPLETED ===");
            
        } catch (Exception e) {
            log.error("Error parsing/saving suggestion: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return convertToResponse(serviceOrder);
    }

    /**
     * Get service suggestions for a service order (return DTO to avoid lazy loading)
     */
    public List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse> getServiceSuggestions(UUID serviceOrderId) {
        log.debug("Getting service suggestions for Service Order ID: {}", serviceOrderId);
        
        List<ServiceSuggestion> suggestions = serviceSuggestionRepository.findByServiceOrderId(serviceOrderId);
        
        log.info("Found {} service suggestions for Service Order ID: {}", suggestions.size(), serviceOrderId);
        
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
        
        // Lấy thông tin customer từ appointment
        try {
            if (serviceOrder.getAppointment() != null && serviceOrder.getAppointment().getCustomer() != null) {
                Customer customer = serviceOrder.getAppointment().getCustomer();
                ServiceOrderResponse.CustomerInfo customerInfo = new ServiceOrderResponse.CustomerInfo();
                customerInfo.setFirstName(customer.getFullName() != null ? customer.getFullName().split(" ")[0] : "");
                customerInfo.setLastName(customer.getFullName() != null && customer.getFullName().split(" ").length > 1 
                    ? customer.getFullName().substring(customer.getFullName().indexOf(" ") + 1) : "");
                customerInfo.setEmail(customer.getEmail());
                customerInfo.setPhone(customer.getPhone());
                response.setCustomer(customerInfo);
            }
        } catch (Exception e) {
            log.error("Error loading customer info: {}", e.getMessage());
        }
        
        // Lấy thông tin vehicle từ appointment
        try {
            if (serviceOrder.getAppointment() != null && serviceOrder.getAppointment().getVehicle() != null) {
                Vehicle vehicle = serviceOrder.getAppointment().getVehicle();
                ServiceOrderResponse.VehicleInfo vehicleInfo = new ServiceOrderResponse.VehicleInfo();
                if (vehicle.getVehicleModel() != null) {
                    vehicleInfo.setModel(vehicle.getVehicleModel().getModel());
                    vehicleInfo.setManufacturer(vehicle.getVehicleModel().getManufacturer());
                }
                vehicleInfo.setLicensePlate(vehicle.getLicensePlate());
                response.setVehicle(vehicleInfo);
            }
        } catch (Exception e) {
            log.error("Error loading vehicle info: {}", e.getMessage());
        }
        
        // Lấy thông tin technician từ technicianUserId
        if (serviceOrder.getTechnicianUserId() != null) {
            response.setTechnicianId(serviceOrder.getTechnicianUserId());
            // Nếu có quan hệ technician được lazy load
            try {
                if (serviceOrder.getTechnician() != null) {
                    User techUser = serviceOrder.getTechnician().getUser();
                    response.setTechnicianName(techUser.getFullName());
                    
                    ServiceOrderResponse.TechnicianInfo technicianInfo = new ServiceOrderResponse.TechnicianInfo();
                    
                    // Split fullName into firstName and lastName
                    String fullName = techUser.getFullName() != null ? techUser.getFullName() : "";
                    String[] nameParts = fullName.trim().split("\\s+", 2);
                    technicianInfo.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
                    technicianInfo.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                    
                    technicianInfo.setEmail(techUser.getEmail());
                    technicianInfo.setPhone(techUser.getPhone());
                    response.setTechnician(technicianInfo);
                }
            } catch (Exception e) {
                // LazyInitializationException - skip technician name
                log.error("Error loading technician info: {}", e.getMessage());
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

