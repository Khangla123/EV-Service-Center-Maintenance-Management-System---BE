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
import com.swp391.EV.service.repository.ServiceOrderPartRepository;
import com.swp391.EV.service.repository.ServiceSuggestionRepository;
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
    private final ServiceOrderPartRepository serviceOrderPartRepository;
    private final ServiceSuggestionRepository serviceSuggestionRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // Fetch ServiceOrder with all necessary relationships to avoid lazy loading issues
        ServiceOrder serviceOrder = serviceOrderRepository.findByIdWithRelations(request.getServiceOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_ORDER_NOT_FOUND));

        // Get customer from service order
        Customer customer = serviceOrder.getAppointment().getCustomer();

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Calculate subtotal from service order
        BigDecimal subtotal = request.getSubtotal();
        
        // If subtotal not provided, calculate from service package + parts
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("===========================");
            System.out.println("💰 CALCULATING INVOICE AMOUNT");
            
            // 1. Get service package price
            BigDecimal servicePrice = BigDecimal.ZERO;
            if (serviceOrder.getAppointment() != null 
                && serviceOrder.getAppointment().getServicePackage() != null) {
                servicePrice = serviceOrder.getAppointment().getServicePackage().getPrice();
                System.out.println("Service package price: " + servicePrice);
            }
            
            // 2. Get parts total from service_order_parts
            BigDecimal partsTotal = serviceOrderPartRepository.findByServiceOrderId(serviceOrder.getId())
                .stream()
                .map(part -> {
                    BigDecimal partTotal = part.getTotalPrice();
                    System.out.println("Part: " + part.getPart().getName() 
                        + " x " + part.getQuantity() 
                        + " = " + partTotal);
                    return partTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("Parts total: " + partsTotal);
            
            // 3. Get approved service suggestions total
            BigDecimal suggestionsTotal = serviceSuggestionRepository
                .findByServiceOrderIdAndStatus(serviceOrder.getId(), 
                    com.swp391.EV.service.model.ServiceSuggestion.SuggestionStatus.APPROVED)
                .stream()
                .map(suggestion -> {
                    BigDecimal suggestionCost = suggestion.getEstimatedCost();
                    System.out.println("Approved suggestion: " + suggestion.getServiceName() 
                        + " = " + suggestionCost);
                    return suggestionCost;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("Approved suggestions total: " + suggestionsTotal);
            
            subtotal = servicePrice.add(partsTotal).add(suggestionsTotal);
            System.out.println("Subtotal (service + parts + approved suggestions): " + subtotal);
            System.out.println("===========================");
        }

        // Calculate total amount
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);

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
        
        // Fetch invoice again with all relationships to avoid lazy loading issues
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
    
    public Invoice getInvoiceByServiceOrderId(UUID serviceOrderId) {
        List<Invoice> invoices = invoiceRepository.findByServiceOrderId(serviceOrderId);
        return invoices.isEmpty() ? null : invoices.get(0);
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
            
            // Get appointment info
            if (invoice.getServiceOrder().getAppointment() != null) {
                response.setAppointmentId(invoice.getServiceOrder().getAppointment().getId());
                
                // Get vehicle info from appointment
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
        
        // Set aliases for frontend compatibility
        response.setFinalAmount(invoice.getTotalAmount());
        response.setIssueDate(invoice.getIssuedAt());
        response.setDiscount(invoice.getDiscountAmount());
        response.setStatus(invoice.getStatus());

        return response;
    }
}
