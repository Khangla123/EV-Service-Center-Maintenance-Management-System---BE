package com.swp391.EV.service.dto.request;

import com.swp391.EV.service.model.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    private UUID invoiceId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private String referenceNumber;
}

