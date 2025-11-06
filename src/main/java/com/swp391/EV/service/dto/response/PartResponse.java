package com.swp391.EV.service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponse {
    private UUID id;
    private UUID serviceCenterId;
    private String serviceCenterName;
    private String partCode;
    private String name;
    private String description;
    private String category;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String supplier;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
