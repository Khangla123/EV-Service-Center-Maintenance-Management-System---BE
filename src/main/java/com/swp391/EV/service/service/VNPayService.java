package com.swp391.EV.service.service;

import com.swp391.EV.service.config.VNPayConfig;
import com.swp391.EV.service.dto.response.PaymentResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Invoice;
import com.swp391.EV.service.model.Payment;
import com.swp391.EV.service.repository.InvoiceRepository;
import com.swp391.EV.service.repository.PaymentRepository;
import com.swp391.EV.service.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Tạo URL thanh toán VNPay
     * @param invoiceId ID của hóa đơn cần thanh toán
     * @param amount Số tiền cần thanh toán (VNĐ)
     * @param orderInfo Thông tin đơn hàng
     * @param ipAddress IP của khách hàng
     * @return URL để redirect khách hàng đến trang thanh toán VNPay
     */
    public String createPaymentUrl(UUID invoiceId, long amount, String orderInfo, String ipAddress) throws UnsupportedEncodingException {
        // Log config để debug
        System.out.println("VNPay Config - TMN Code: " + vnPayConfig.getTmnCode());
        System.out.println("VNPay Config - Secret Key: " + vnPayConfig.getSecretKey());
        System.out.println("VNPay Config - URL: " + vnPayConfig.getVnpUrl());
        
        // Verify invoice exists
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Tạo txnRef (mã giao dịch duy nhất)
        String txnRef = VNPayUtil.getRandomNumber(8);

        // Tạo payment record với status PENDING
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(java.math.BigDecimal.valueOf(amount))
                .paymentMethod(Payment.PaymentMethod.E_WALLET)
                .status(Payment.PaymentStatus.PENDING)
                .transactionId(txnRef)
                .build();
        paymentRepository.save(payment);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, vnPayConfig.getTimeoutMinutes());
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build hash data and query string - IMPORTANT: Both must be URL encoded per VNPay spec
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // URL encode both key and value for hash data
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString());
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                
                // Hash data: encodedKey=encodedValue (URL encoded per VNPay PHP sample)
                hashData.append(encodedName).append('=').append(encodedValue);
                
                // Query string: same encoding
                query.append(encodedName).append('=').append(encodedValue);
                
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        String queryUrl = query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
        
        String fullUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;
        System.out.println("=== VNPay Payment URL ===");
        System.out.println("Hash Data: " + hashData.toString());
        System.out.println("Secure Hash: " + vnpSecureHash);
        System.out.println("Full URL: " + fullUrl);
        
        return fullUrl;
    }

    /**
     * Xử lý callback từ VNPay sau khi khách hàng thanh toán
     * @param params Các tham số VNPay trả về
     * @return PaymentResponse với thông tin kết quả thanh toán
     */
    @Transactional
    public PaymentResponse processCallback(Map<String, String> params) {
        System.out.println("=== VNPAY CALLBACK RECEIVED ===");
        System.out.println("All params: " + params);
        
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Verify signature
        String signValue = VNPayUtil.hashAllFields(params, vnPayConfig.getSecretKey());
        System.out.println("Calculated hash: " + signValue);
        System.out.println("Received hash: " + vnpSecureHash);

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        System.out.println("Transaction ref: " + txnRef + ", Response code: " + responseCode);

        // Find payment by transaction ID
        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> txnRef.equals(p.getTransactionId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        
        System.out.println("Found payment ID: " + payment.getId());

        if (signValue.equals(vnpSecureHash)) {
            if ("00".equals(responseCode)) {
                // Thanh toán thành công - cập nhật cả Payment và Invoice
                System.out.println("Payment SUCCESS - updating statuses...");
                payment.setStatus(Payment.PaymentStatus.PAID);
                payment.setPaymentDate(LocalDateTime.now());
                
                // Cập nhật Invoice status thành PAID
                Invoice invoice = payment.getInvoice();
                System.out.println("Invoice ID: " + invoice.getId() + ", Status before: " + invoice.getStatus());
                invoice.setStatus(Invoice.InvoiceStatus.PAID);
                invoiceRepository.save(invoice);
                System.out.println("Invoice status after save: " + invoice.getStatus());
            } else {
                // Thanh toán thất bại
                System.out.println("Payment FAILED - code: " + responseCode);
                payment.setStatus(Payment.PaymentStatus.FAILED);
            }
            payment = paymentRepository.save(payment);
            System.out.println("Payment saved with status: " + payment.getStatus());
        } else {
            System.out.println("ERROR: Invalid signature!");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        System.out.println("=== CALLBACK COMPLETE ===");

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
}

