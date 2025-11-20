package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateInvoiceRequest;
import com.swp391.EV.service.dto.request.UpdateInvoiceRequest;
import com.swp391.EV.service.dto.response.InvoiceResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.Invoice;
import com.swp391.EV.service.model.ServiceOrder;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.InvoiceRepository;
import com.swp391.EV.service.repository.ServiceOrderRepository;
import com.swp391.EV.service.repository.ServiceOrderPartRepository;
import com.swp391.EV.service.repository.ServiceSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    @Autowired
    private final InvoiceRepository invoiceRepository;
    @Autowired
    private final ServiceOrderRepository serviceOrderRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final ServiceOrderPartRepository serviceOrderPartRepository;
    @Autowired
    private final ServiceSuggestionRepository serviceSuggestionRepository;
    @Autowired
    private final ModelMapper modelMapper;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // Lấy ServiceOrder với tất cả các mối quan hệ để tránh lỗi lazy loading
        ServiceOrder serviceOrder = serviceOrderRepository.findByIdWithRelations(request.getServiceOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));
        Customer customer = serviceOrder.getAppointment().getCustomer();
        String invoiceNumber = generateInvoiceNumber();
        BigDecimal subtotal = request.getSubtotal();

        // Nếu subtotal không được cung cấp, tính toán từ gói dịch vụ + phụ tùng
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
            // 1. Lấy giá gói dịch vụ
            BigDecimal servicePrice = BigDecimal.ZERO;
            if (serviceOrder.getAppointment() != null 
                && serviceOrder.getAppointment().getServicePackage() != null) {
                servicePrice = serviceOrder.getAppointment().getServicePackage().getPrice();
            }
            
            // 2. Lấy tổng tiền phụ tùng từ bảng service_order_parts
            BigDecimal partsTotal = serviceOrderPartRepository.findByServiceOrderId(serviceOrder.getId())
                .stream()
                .map(part -> part.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. Lấy tổng tiền dịch vụ đề xuất đã được phê duyệt
            BigDecimal suggestionsTotal = serviceSuggestionRepository
                .findByServiceOrderIdAndStatus(serviceOrder.getId(), 
                    com.swp391.EV.service.model.ServiceSuggestion.SuggestionStatus.APPROVED)
                .stream()
                .map(suggestion -> suggestion.getEstimatedCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            subtotal = servicePrice.add(partsTotal).add(suggestionsTotal);
        }

        // Tính tổng số tiền cuối cùng
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        Invoice invoice = Invoice.builder()
                .serviceOrder(serviceOrder)
                .customer(customer)
                .invoiceNumber(invoiceNumber)
                .subtotal(request.getSubtotal())
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .issuedAt(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();

        invoice = invoiceRepository.save(invoice);
        
        // Lấy lại invoice với tất cả các mối quan hệ để tránh lỗi lazy loading
        invoice = invoiceRepository.findByIdWithRelations(invoice.getId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        
        return mapToResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAllWithRelations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return mapToResponse(invoice);
    }

    public List<InvoiceResponse> getMyInvoices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return invoiceRepository.findByCustomerIdWithRelations(customer.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    

    public List<InvoiceResponse> getUnpaidInvoices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return invoiceRepository.findByCustomerId(customer.getId()).stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.PENDING 
                                || invoice.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponse updateInvoice(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (request.getSubtotal() != null) {
            invoice.setSubtotal(request.getSubtotal());
        }
        if (request.getDiscountAmount() != null) {
            invoice.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }

        // Tính lại tổng số tiền (không bao gồm thuế)
        BigDecimal totalAmount = invoice.getSubtotal()
                .subtract(invoice.getDiscountAmount());
        invoice.setTotalAmount(totalAmount);

        invoice = invoiceRepository.save(invoice);
        return mapToResponse(invoice);
    }

    private String generateInvoiceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "INV-" + timestamp.substring(timestamp.length() - 10);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        InvoiceResponse response = modelMapper.map(invoice, InvoiceResponse.class);

        if (invoice.getServiceOrder() != null) {
            response.setServiceOrderId(invoice.getServiceOrder().getId());
            response.setOrderCode(invoice.getServiceOrder().getOrderCode());
            
            // Lấy thông tin appointment
            if (invoice.getServiceOrder().getAppointment() != null) {
                response.setAppointmentId(invoice.getServiceOrder().getAppointment().getId());
                
                // Lấy thông tin xe từ appointment
                if (invoice.getServiceOrder().getAppointment().getVehicle() != null) {
                    response.setVehicleLicensePlate(
                        invoice.getServiceOrder().getAppointment().getVehicle().getLicensePlate()
                    );
                }
            }
        }

        if (invoice.getCustomer() != null) {
            response.setCustomerId(invoice.getCustomer().getId());
            response.setCustomerName(invoice.getCustomer().getFullName());
        }
        
        // Set các alias để tương thích với frontend
        response.setFinalAmount(invoice.getTotalAmount());
        response.setIssueDate(invoice.getIssuedAt());
        response.setDiscount(invoice.getDiscountAmount());
        response.setStatus(invoice.getStatus());

        return response;
    }
}
