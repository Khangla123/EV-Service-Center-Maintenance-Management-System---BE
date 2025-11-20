package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreatePaymentRequest;
import com.swp391.EV.service.dto.request.VerifyPaymentRequest;
import com.swp391.EV.service.dto.response.PaymentResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Invoice;
import com.swp391.EV.service.model.Payment;
import com.swp391.EV.service.repository.InvoiceRepository;
import com.swp391.EV.service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Autowired
    private final PaymentRepository paymentRepository;
    @Autowired
    private final InvoiceRepository invoiceRepository;
    @Autowired
    private final ModelMapper modelMapper;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Verify invoice exists
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Generate transaction ID
        String transactionId = generateTransactionId();

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .transactionId(transactionId)
                .referenceNumber(request.getReferenceNumber())
                .build();

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse verifyPayment(UUID id, VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
            if (request.getStatus() == Payment.PaymentStatus.PAID) {
                payment.setPaymentDate(LocalDateTime.now());
            }
        }

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getAvailableMethods() {
        // Return available payment methods
        return List.of(
            PaymentResponse.builder()
                .paymentMethod(Payment.PaymentMethod.CASH)
                .build(),
            PaymentResponse.builder()
                .paymentMethod(Payment.PaymentMethod.CARD)
                .build(),
            PaymentResponse.builder()
                .paymentMethod(Payment.PaymentMethod.E_WALLET)
                .build(),
            PaymentResponse.builder()
                .paymentMethod(Payment.PaymentMethod.BANKING)
                .build()
        );
    }

    private String generateTransactionId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "TXN-" + timestamp;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = modelMapper.map(payment, PaymentResponse.class);

        if (payment.getInvoice() != null) {
            response.setInvoiceId(payment.getInvoice().getId());
            response.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        }

        return response;
    }
}

