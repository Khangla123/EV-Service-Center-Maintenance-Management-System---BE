package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreatePaymentRequest;
import com.swp391.EV.service.dto.request.VerifyPaymentRequest;
import com.swp391.EV.service.dto.response.PaymentResponse;
import com.swp391.EV.service.service.PaymentService;
import com.swp391.EV.service.service.VNPayService;
import com.swp391.EV.service.service.MockPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment Management APIs")
public class PaymentController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final MockPaymentService mockPaymentService;

    @GetMapping
    @Operation(summary = "Danh sách thanh toán", description = "Payment history - Get all payments (STAFF/ADMIN)")
    public ApiResponse<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> responses = paymentService.getAllPayments();
        return ApiResponse.<List<PaymentResponse>>builder()
                .message("Danh sách thanh toán.")
                .result(responses)
                .build();
    }

    @PostMapping
    @Operation(summary = "Thanh toán", description = "Payment processing - Create new payment (CUSTOMER/STAFF)")
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ApiResponse.<PaymentResponse>builder()
                .message("Tạo thanh toán thành công.")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết thanh toán", description = "Payment details - Get payment by ID (STAFF/ADMIN/CUSTOMER)")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable UUID id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ApiResponse.<PaymentResponse>builder()
                .message("Chi tiết thanh toán.")
                .result(response)
                .build();
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Xác nhận thanh toán", description = "Manual verification - Verify payment status (STAFF/ADMIN)")
    public ApiResponse<PaymentResponse> verifyPayment(
            @PathVariable UUID id,
            @RequestBody VerifyPaymentRequest request) {
        PaymentResponse response = paymentService.verifyPayment(id, request);
        return ApiResponse.<PaymentResponse>builder()
                .message("Xác nhận thanh toán thành công.")
                .result(response)
                .build();
    }

    @GetMapping("/methods")
    @Operation(summary = "Phương thức thanh toán", description = "Available methods - Get available payment methods (CUSTOMER)")
    public ApiResponse<List<PaymentResponse>> getAvailableMethods() {
        List<PaymentResponse> responses = paymentService.getAvailableMethods();
        return ApiResponse.<List<PaymentResponse>>builder()
                .message("Phương thức thanh toán.")
                .result(responses)
                .build();
    }

    @PostMapping("/vnpay/create")
    @Operation(summary = "Tạo URL thanh toán VNPay", description = "Create VNPay payment URL - Tạo link để redirect customer đến trang thanh toán VNPay")
    public ApiResponse<String> createVNPayPayment(
            @RequestParam UUID invoiceId,
            @RequestParam long amount,
            @RequestParam(required = false, defaultValue = "Thanh toán hóa đơn") String orderInfo,
            HttpServletRequest request) throws UnsupportedEncodingException {

        String ipAddress = getClientIP(request);
        String paymentUrl = vnPayService.createPaymentUrl(invoiceId, amount, orderInfo, ipAddress);

        return ApiResponse.<String>builder()
                .message("Tạo URL thanh toán VNPay thành công.")
                .result(paymentUrl)
                .build();
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay callback", description = "Handle VNPay payment callback - Xử lý kết quả thanh toán từ VNPay")
    public ApiResponse<PaymentResponse> handleVNPayCallback(@RequestParam Map<String, String> params) {
        PaymentResponse response = vnPayService.processCallback(params);
        return ApiResponse.<PaymentResponse>builder()
                .message(response.getStatus().toString().equals("PAID")
                        ? "Thanh toán thành công."
                        : "Thanh toán thất bại.")
                .result(response)
                .build();
    }

    // ========== MOCK PAYMENT ENDPOINTS (Thanh toán giả lập) ==========
    
    @PostMapping("/mock/create")
    @Operation(summary = "Tạo URL thanh toán giả lập", description = "Create mock payment URL - Tạo link giả lập cho mục đích demo/testing")
    public ApiResponse<String> createMockPayment(
            @RequestParam UUID invoiceId,
            @RequestParam long amount,
            @RequestParam(required = false, defaultValue = "Thanh toán hóa đơn") String orderInfo) {

        String mockPaymentUrl = mockPaymentService.createMockPaymentUrl(invoiceId, amount, orderInfo);

        return ApiResponse.<String>builder()
                .message("Tạo URL thanh toán giả lập thành công.")
                .result(mockPaymentUrl)
                .build();
    }

    @PostMapping("/mock/callback")
    @Operation(summary = "Mock payment callback", description = "Handle mock payment callback - Xử lý kết quả thanh toán giả lập")
    public ApiResponse<PaymentResponse> handleMockCallback(
            @RequestParam String txnRef,
            @RequestParam boolean success) {
        
        PaymentResponse response = mockPaymentService.processMockCallback(txnRef, success);
        
        return ApiResponse.<PaymentResponse>builder()
                .message(response.getStatus().toString().equals("PAID")
                        ? "Thanh toán giả lập thành công."
                        : "Thanh toán giả lập thất bại.")
                .result(response)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
