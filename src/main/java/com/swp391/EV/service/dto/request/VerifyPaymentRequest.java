package com.swp391.EV.service.dto.request;

import com.swp391.EV.service.model.Payment;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentRequest {
    private String transactionId;
    private Payment.PaymentStatus status;
}

