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
import org.springframework.beans.factory.annotation.Autowired;
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

    // Constants cho log messages
    private static final String SERVICE_ORDER_FOUND = "Đã tìm thấy Service Order: {}";
    private static final String SERVICE_ORDER_ID_LOG = "Service Order ID: {}";
    @Autowired
    private final ServiceOrderRepository serviceOrderRepository;
    @Autowired
    private final ServiceAppointmentRepository appointmentRepository;
    @Autowired
    private final StaffRepository staffRepository;
    @Autowired
    private final MaintenancePlanRepository maintenancePlanRepository;
    @Autowired
    private final ServiceOrderPartRepository serviceOrderPartRepository;
    @Autowired
    private final PartRepository partRepository;
    @Autowired
    private final ServiceSuggestionRepository serviceSuggestionRepository;

    public List<ServiceOrderResponse> getAllServiceOrders() {
        // Sử dụng JOIN FETCH query để eager load customer và vehicle
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

        // Technician có thể null khi tạo mới (chưa phân công)
        UUID technicianUserId = null;
        if (request.getTechnicianId() != null) {
            // Tìm staff theo ID để kiểm tra và lấy user.id
            Staff technicianStaff = staffRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            technicianUserId = technicianStaff.getUser().getId();
        }

        // Tạo mã order code
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technicianUserId(technicianUserId) // Lưu user.id vào DB
                // LƯU Ý: Status đã bỏ - được quản lý trong appointment.status
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

        // 2. Kiểm tra appointment đã được xác nhận chưa
        if (appointment.getStatus() != ServiceAppointment.AppointmentStatus.CONFIRMED) {
            log.error("Trạng thái appointment không phải CONFIRMED! Trạng thái hiện tại: {}", appointment.getStatus());
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Kiểm tra appointment đã có service order chưa (tránh tạo trùng)
        boolean hasExistingOrder = serviceOrderRepository.findByAppointmentId(appointmentId).isPresent();

        if (hasExistingOrder) {
            log.error("Appointment đã có service order rồi!");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 4. Lấy thông tin kỹ thuật viên
        // QUAN TRỌNG: technicianId là USER_ID từ frontend
        Staff technicianStaff = staffRepository.findByUserId(technicianId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        
        UUID technicianUserId = technicianStaff.getUser().getId();

        // 5. Load checklist templates từ maintenance_plans cho TẤT CẢ các gói đã chọn
        String checklistJson = null;
        try {
            UUID vehicleModelId = appointment.getVehicle().getVehicleModel().getId();
            
            // Parse selected_packages JSON array để lấy tất cả package IDs
            java.util.List<UUID> selectedPackageIds = new java.util.ArrayList<>();
            
            if (appointment.getSelectedPackages() != null && !appointment.getSelectedPackages().trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> packageIdStrings = mapper.readValue(
                    appointment.getSelectedPackages(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {}
                );
                
                for (String pkgIdStr : packageIdStrings) {
                    selectedPackageIds.add(UUID.fromString(pkgIdStr));
                }
            } else {
                // Fallback: Sử dụng service_package_id đơn nếu selected_packages rỗng
                UUID fallbackPackageId = appointment.getServicePackage().getId();
                selectedPackageIds.add(fallbackPackageId);
                log.warn("Không tìm thấy selected_packages, sử dụng service_package_id dự phòng: {}", fallbackPackageId);
            }
            
            // Load và merge checklist templates từ tất cả các packages
            java.util.List<java.util.Map<String, Object>> mergedChecklist = new java.util.ArrayList<>();
            int orderCounter = 1;
            
            for (UUID packageId : selectedPackageIds) {
                Optional<MaintenancePlan> maintenancePlan = maintenancePlanRepository
                        .findByServicePackageIdAndVehicleModelId(packageId, vehicleModelId);
                
                if (maintenancePlan.isEmpty()) {
                    // Fallback: Tìm plan chung không phân biệt vehicle model
                    maintenancePlan = maintenancePlanRepository.findByServicePackageId(packageId);
                }
                
                if (maintenancePlan.isPresent()) {
                    String templateJson = maintenancePlan.get().getChecklistTemplate();

                    // Parse checklist JSON array
                    if (templateJson != null && !templateJson.trim().isEmpty()) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.List<java.util.Map<String, Object>> checklistItems = mapper.readValue(
                            templateJson,
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                        );
                        
                        // Re-number order field để tránh xung đột khi merge
                        for (java.util.Map<String, Object> item : checklistItems) {
                            item.put("order", orderCounter++);
                        }
                        
                        mergedChecklist.addAll(checklistItems);
                    }
                } else {
                    log.warn("Không tìm thấy maintenance plan cho package ID: {}", packageId);
                }
            }
            
            // Chuyển merged checklist trở lại thành JSON string
            if (!mergedChecklist.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                checklistJson = mapper.writeValueAsString(mergedChecklist);
            } else {
                log.warn("Không tìm thấy checklist items nào cho các gói đã chọn");
            }
            
        } catch (Exception e) {
            log.error("LỖI khi load/merge checklist templates: {}", e.getMessage(), e);
        }

        // 6. Tạo Service Order mới với technician đã được phân công
        String orderCode = "SO" + System.currentTimeMillis();

        ServiceOrder serviceOrder = ServiceOrder.builder()
                .appointment(appointment)
                .orderCode(orderCode)
                .technician(technicianStaff)
                .checklist(checklistJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        // 7. Cập nhật trạng thái appointment thành ASSIGNED và gán technician
        appointment.setStatus(ServiceAppointment.AppointmentStatus.ASSIGNED);
        appointment.setTechnician(technicianStaff);
        appointmentRepository.save(appointment);

        return convertToResponse(savedOrder);
    }

    /**
     * Lấy service order theo appointment ID
     */
    public ServiceOrderResponse getServiceOrderByAppointmentId(UUID appointmentId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        return convertToResponse(serviceOrder);
    }

    public ServiceOrderResponse updateIssues(UUID serviceOrderId, String issuesJson) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        serviceOrder.setIssues(issuesJson);
        serviceOrder.setUpdatedAt(LocalDateTime.now());
        
        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);
        
        return convertToResponse(savedOrder);
    }

    /**
     * Set giá cho một issue cụ thể (chỉ Staff/Admin)
     * @param serviceOrderId Service order ID
     * @param issueId Issue ID cần cập nhật giá
     * @param price Giá cần set
     * @return Service order đã cập nhật
     */
    @Transactional
    public ServiceOrderResponse setIssuePrice(UUID serviceOrderId, String issueId, Double price) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        try {
            // Parse issues JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> issuesList = mapper.readValue(
                serviceOrder.getIssues() != null ? serviceOrder.getIssues() : "[]",
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );
            
            // Tìm và cập nhật issue cụ thể
            boolean found = false;
            for (java.util.Map<String, Object> issue : issuesList) {
                if (issueId.equals(issue.get("id"))) {
                    issue.put("price", price);
                    issue.put("staffSetPrice", true);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                log.error("Không tìm thấy issue: {}", issueId);
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            // Lưu issues đã cập nhật
            String updatedIssuesJson = mapper.writeValueAsString(issuesList);
            serviceOrder.setIssues(updatedIssuesJson);
            serviceOrder.setUpdatedAt(LocalDateTime.now());
            
            ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

            return convertToResponse(savedOrder);
            
        } catch (Exception e) {
            log.error("Lỗi khi set giá issue: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * Thêm phụ tùng đã sử dụng vào service order
     * @param serviceOrderId Service order ID
     * @param partsJson JSON array: [{"partCode": "PT001", "partName": "...", "quantity": 2, "unit": "cái"}]
     */
    @Transactional
    public ServiceOrderResponse addPartsUsed(UUID serviceOrderId, String partsJson) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        try {
            // Parse JSON thành list thông tin phụ tùng
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> partsList = mapper.readValue(
                partsJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );

            for (java.util.Map<String, Object> partInfo : partsList) {
                String partCode = (String) partInfo.get("partCode");
                Integer quantity = (Integer) partInfo.get("quantity");

                // Tìm phụ tùng theo mã
                Part part = partRepository.findByPartCode(partCode)
                        .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));

                // Tạo bản ghi service_order_part
                ServiceOrderPart orderPart = ServiceOrderPart.builder()
                        .serviceOrder(serviceOrder)
                        .part(part)
                        .quantity(quantity)
                        .unitPrice(part.getUnitPrice())
                        .createdAt(LocalDateTime.now())
                        .build();
                
                serviceOrderPartRepository.save(orderPart);

                // Cập nhật số lượng tồn kho phụ tùng
                int newStock = part.getStockQuantity() - quantity;
                part.setStockQuantity(newStock);
                partRepository.save(part);
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi parse/lưu phụ tùng: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return convertToResponse(serviceOrder);
    }

    /**
     * Lấy số lượng phụ tùng đã sử dụng cho service order (Phiên bản đơn giản - chỉ trả về số lượng)
     * @param serviceOrderId Service order ID
     * @return Số lượng phụ tùng đã sử dụng
     */
    public int getPartsUsedCount(UUID serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        return parts.size();
    }

    /**
     * Lấy tóm tắt phụ tùng (số lượng + tổng giá)
     * @param serviceOrderId Service order ID
     * @return Map với "count" và "total"
     */
    public java.util.Map<String, Object> getPartsUsedSummary(UUID serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);
        int count = parts.size();
        
        BigDecimal total = parts.stream()
                .map(ServiceOrderPart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return java.util.Map.of("count", count, "total", total);
    }

    /**
     * Lấy danh sách chi tiết phụ tùng đã sử dụng
     * @param serviceOrderId Service order ID
     * @return List của ServiceOrderPartResponse
     */
    public List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse> getPartsUsed(UUID serviceOrderId) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        List<ServiceOrderPart> parts = serviceOrderPartRepository.findByServiceOrderId(serviceOrderId);

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
        
        return response;
    }

    /**
     * Thêm dịch vụ đề xuất (dịch vụ bổ sung được khuyến nghị)
     * @param serviceOrderId Service order ID
     * @param suggestionJson JSON: {"serviceName": "...", "reason": "...", "estimatedCost": 500000}
     */
    @Transactional
    public ServiceOrderResponse addServiceSuggestion(UUID serviceOrderId, String suggestionJson) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        
        try {
            // Parse JSON thành thông tin suggestion
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> suggestionData = mapper.readValue(
                suggestionJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
            );
            
            String serviceName = (String) suggestionData.get("serviceName");
            String reason = (String) suggestionData.get("reason");
            Object costObj = suggestionData.get("estimatedCost");
            
            // Xử lý estimatedCost - có thể là String hoặc Number
            BigDecimal estimatedCost = BigDecimal.ZERO;
            if (costObj != null) {
                if (costObj instanceof Number) {
                    estimatedCost = BigDecimal.valueOf(((Number) costObj).doubleValue());
                } else if (costObj instanceof String) {
                    // Xóa các ký tự không phải số trừ dấu chấm
                    String costStr = ((String) costObj).replaceAll("[^0-9.]", "");
                    estimatedCost = new BigDecimal(costStr);
                }
            }

            // Tạo bản ghi service suggestion
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

        } catch (Exception e) {
            log.error("Lỗi khi parse/lưu suggestion: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return convertToResponse(serviceOrder);
    }

    /**
     * Lấy danh sách dịch vụ đề xuất cho service order (trả về DTO để tránh lazy loading)
     */
    public List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse> getServiceSuggestions(UUID serviceOrderId) {
        List<ServiceSuggestion> suggestions = serviceSuggestionRepository.findByServiceOrderId(serviceOrderId);

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
     * Cập nhật trạng thái suggestion (APPROVED/REJECTED)
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
            log.error("Lỗi khi load thông tin customer: {}", e.getMessage());
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
            log.error("Lỗi khi load thông tin vehicle: {}", e.getMessage());
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
                    
                    // Tách fullName thành firstName và lastName
                    String fullName = techUser.getFullName() != null ? techUser.getFullName() : "";
                    String[] nameParts = fullName.trim().split("\\s+", 2);
                    technicianInfo.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
                    technicianInfo.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                    
                    technicianInfo.setEmail(techUser.getEmail());
                    technicianInfo.setPhone(techUser.getPhone());
                    response.setTechnician(technicianInfo);
                }
            } catch (Exception e) {
                // LazyInitializationException - bỏ qua tên technician
                log.error("Lỗi khi load thông tin technician: {}", e.getMessage());
                response.setTechnicianName("Unknown");
            }
        }
        
        // LƯU Ý: Status được lấy từ appointment.status, không lưu ở service_order nữa
        // response.setStatus() - ĐÃ BỎ
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

