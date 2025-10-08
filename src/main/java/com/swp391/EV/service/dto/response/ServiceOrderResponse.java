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
    private ServiceOrder.ServiceStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String checklist;
    private String diagnosis;
    private String workPerformed;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
