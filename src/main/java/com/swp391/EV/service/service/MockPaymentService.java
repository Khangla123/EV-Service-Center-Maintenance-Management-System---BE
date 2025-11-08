package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.response.PaymentResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Invoice;
import com.swp391.EV.service.model.Payment;
import com.swp391.EV.service.repository.InvoiceRepository;
import com.swp391.EV.service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Payment Service - Giả lập thanh toán không cần gateway thật
 * Tạo URL giả lập và xử lý callback giả lập cho mục đích demo/testing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MockPaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    // Store pending payments in memory
    private final Map<String, UUID> pendingPayments = new ConcurrentHashMap<>();

    /**
     * Tạo URL thanh toán giả lập
     * @param invoiceId ID của hóa đơn cần thanh toán
     * @param amount Số tiền cần thanh toán (VNĐ)
     * @param orderInfo Thông tin đơn hàng
     * @return URL giả lập để redirect đến trang thanh toán mock
     */
    public String createMockPaymentUrl(UUID invoiceId, long amount, String orderInfo) {
        log.info("Creating mock payment URL for invoice: {}, amount: {}", invoiceId, amount);

        // Verify invoice exists
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Tạo transaction ID giả lập
        String txnRef = "MOCK" + System.currentTimeMillis();

        // Tạo payment record với status PENDING
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(java.math.BigDecimal.valueOf(amount))
                .paymentMethod(Payment.PaymentMethod.E_WALLET)
                .status(Payment.PaymentStatus.PENDING)
                .transactionId(txnRef)
                .build();
        paymentRepository.save(payment);

        // Store payment ID for later retrieval
        pendingPayments.put(txnRef, payment.getId());

        // Tạo URL giả lập redirect về frontend với transaction info
        // Dùng route PUBLIC không cần authentication
        String mockPaymentUrl = String.format(
            "http://localhost:3000/mock-payment?txnRef=%s&amount=%d&orderInfo=%s&invoiceId=%s",
            txnRef,
            amount,
            orderInfo.replace(" ", "+"),
            invoiceId.toString()
        );

        log.info("Mock payment URL created: {}", mockPaymentUrl);
        return mockPaymentUrl;
    }

    /**
     * Xử lý callback giả lập - Cho phép customer chọn thành công hoặc thất bại
     * @param txnRef Transaction reference
     * @param success True nếu thanh toán thành công, False nếu thất bại
     * @return PaymentResponse với thông tin kết quả thanh toán
     */
    @Transactional
    public PaymentResponse processMockCallback(String txnRef, boolean success) {
        log.info("========== MOCK CALLBACK START ==========");
        log.info("Processing mock callback for txnRef: {}, success: {}", txnRef, success);

        // Find payment by transaction ID
        log.info("Searching for payment with txnRef: {}", txnRef);
        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> txnRef.equals(p.getTransactionId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("❌ Payment NOT FOUND for txnRef: {}", txnRef);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        log.info("✅ Payment FOUND: ID={}, Current Status={}", payment.getId(), payment.getStatus());

        Payment.PaymentStatus oldStatus = payment.getStatus();
        
        if (success) {
            // Thanh toán thành công
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaymentDate(LocalDateTime.now());
            log.info("✅ Setting payment {} to PAID", payment.getId());
        } else {
            // Thanh toán thất bại
            payment.setStatus(Payment.PaymentStatus.FAILED);
            log.info("❌ Setting payment {} to FAILED", payment.getId());
        }

        log.info("Saving payment to database... (Old status: {}, New status: {})", oldStatus, payment.getStatus());
        payment = paymentRepository.save(payment);
        log.info("✅ Payment SAVED to DB: ID={}, Status={}", payment.getId(), payment.getStatus());

        // Update Invoice status if payment is successful
        if (success && payment.getInvoice() != null) {
            Invoice invoice = payment.getInvoice();
            Invoice.InvoiceStatus oldInvoiceStatus = invoice.getStatus();
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
            log.info("✅ Invoice UPDATED: ID={}, Old Status={}, New Status=PAID", invoice.getId(), oldInvoiceStatus);
        }

        // Remove from pending map
        pendingPayments.remove(txnRef);
        log.info("========== MOCK CALLBACK END ==========");

        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Lấy thông tin payment từ transaction reference
     */
    public Payment getPaymentByTxnRef(String txnRef) {
        return paymentRepository.findAll().stream()
                .filter(p -> txnRef.equals(p.getTransactionId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
