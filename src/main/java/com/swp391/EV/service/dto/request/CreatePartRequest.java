package com.swp391.EV.service.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePartRequest {
    private UUID serviceCenterId;
    private String partCode;
    private String name;
    private String description;
    private String category;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String supplier;
}

