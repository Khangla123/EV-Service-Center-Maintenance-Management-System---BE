package com.swp391.EV.service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private UUID id;
    private UUID serviceOrderId;
    private String orderCode;
    private UUID customerId;
    private String customerName;
    private String invoiceNumber;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private LocalDateTime issuedAt;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}

