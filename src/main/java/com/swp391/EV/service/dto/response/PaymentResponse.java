package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String referenceNumber;
    private LocalDateTime createdAt;
}

