package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.ServiceOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ServiceOrderResponse {
    private UUID id;
    private UUID appointmentId;
    private String orderCode;
    private UUID technicianId;
    private String technicianName;
    // NOTE: Status được quản lý ở appointment.status, không duplicate ở service_order
    // private ServiceOrder.ServiceStatus status; - REMOVED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String checklist;
    private String issues; // JSON string - list of detected issues/problems
    private String diagnosis;
    private String workPerformed;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested objects for customer and vehicle info
    private CustomerInfo customer;
    private VehicleInfo vehicle;
    private TechnicianInfo technician;
    
    @Data
    public static class CustomerInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }
    
    @Data
    public static class VehicleInfo {
        private String model;
        private String manufacturer;
        private String licensePlate;
    }
    
    @Data
    public static class TechnicianInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }
}
