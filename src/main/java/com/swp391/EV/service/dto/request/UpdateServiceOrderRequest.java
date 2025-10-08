package com.swp391.EV.service.dto.request;

import com.swp391.EV.service.model.ServiceOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateServiceOrderRequest {
    private ServiceOrder.ServiceStatus status;
    private UUID technicianId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String checklist;
    private String diagnosis;
    private String workPerformed;
    private BigDecimal totalAmount;
}
