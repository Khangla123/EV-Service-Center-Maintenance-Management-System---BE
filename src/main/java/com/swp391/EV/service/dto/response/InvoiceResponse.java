package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.Invoice;
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
    private UUID appointmentId; // Added for easy filtering
    private String orderCode;
    private UUID customerId;
    private String customerName;
    private String vehicleLicensePlate;
    private String invoiceNumber;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount; // totalAmount after all calculations
    private LocalDateTime issuedAt;
    private LocalDateTime issueDate; // Alias for frontend compatibility
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private Invoice.InvoiceStatus status;
    private String notes;
    private BigDecimal discount; // Alias for discountAmount
}

