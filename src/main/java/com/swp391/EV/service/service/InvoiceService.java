package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateInvoiceRequest;
import com.swp391.EV.service.dto.request.UpdateInvoiceRequest;
import com.swp391.EV.service.dto.response.InvoiceResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.Invoice;
import com.swp391.EV.service.model.ServiceOrder;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.InvoiceRepository;
import com.swp391.EV.service.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    private final InvoiceRepository invoiceRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // Verify service order exists
        ServiceOrder serviceOrder = serviceOrderRepository.findById(request.getServiceOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        // Get customer from service order
        Customer customer = serviceOrder.getAppointment().getCustomer();

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Calculate total amount
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = request.getSubtotal().add(taxAmount).subtract(discountAmount);

        Invoice invoice = Invoice.builder()
                .serviceOrder(serviceOrder)
                .customer(customer)
                .invoiceNumber(invoiceNumber)
                .subtotal(request.getSubtotal())
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .issuedAt(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();

        invoice = invoiceRepository.save(invoice);
        return mapToResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return mapToResponse(invoice);
    }

    public List<InvoiceResponse> getMyInvoices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return invoiceRepository.findByCustomerId(customer.getId()).stream()
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
        if (request.getTaxAmount() != null) {
            invoice.setTaxAmount(request.getTaxAmount());
        }
        if (request.getDiscountAmount() != null) {
            invoice.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }

        // Recalculate total amount
        BigDecimal totalAmount = invoice.getSubtotal()
                .add(invoice.getTaxAmount())
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
        }

        if (invoice.getCustomer() != null) {
            response.setCustomerId(invoice.getCustomer().getId());
            response.setCustomerName(invoice.getCustomer().getFullName());
        }

        return response;
    }
}
