package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateInvoiceRequest;
import com.swp391.EV.service.dto.request.UpdateInvoiceRequest;
import com.swp391.EV.service.dto.response.InvoiceResponse;
import com.swp391.EV.service.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice Management APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "Danh sách hóa đơn", description = "Filter by customer - Get all invoices (STAFF/ADMIN)")
    public ApiResponse<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> responses = invoiceService.getAllInvoices();
        return ApiResponse.<List<InvoiceResponse>>builder()
                .message("Danh sách hóa đơn.")
                .result(responses)
                .build();
    }

    @PostMapping
    @Operation(summary = "Tạo hóa đơn", description = "From service order - Create invoice from completed service (STAFF)")
    public ApiResponse<InvoiceResponse> createInvoice(@RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ApiResponse.<InvoiceResponse>builder()
                .message("Tạo hóa đơn thành công.")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết hóa đơn", description = "Invoice details - Get invoice by ID (STAFF/ADMIN/CUSTOMER)")
    public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ApiResponse.<InvoiceResponse>builder()
                .message("Chi tiết hóa đơn.")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật hóa đơn", description = "Invoice modification - Update invoice details (STAFF/ADMIN)")
    public ApiResponse<InvoiceResponse> updateInvoice(
            @PathVariable UUID id,
            @RequestBody UpdateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ApiResponse.<InvoiceResponse>builder()
                .message("Cập nhật hóa đơn thành công.")
                .result(response)
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Hóa đơn của tôi", description = "Customer's invoices - Get invoices for current customer (CUSTOMER)")
    public ApiResponse<List<InvoiceResponse>> getMyInvoices() {
        List<InvoiceResponse> responses = invoiceService.getMyInvoices();
        return ApiResponse.<List<InvoiceResponse>>builder()
                .message("Hóa đơn của tôi.")
                .result(responses)
                .build();
    }
    
    @GetMapping("/unpaid")
    @Operation(summary = "Hóa đơn chưa thanh toán", description = "Get unpaid invoices for current customer (CUSTOMER)")
    public ApiResponse<List<InvoiceResponse>> getUnpaidInvoices() {
        List<InvoiceResponse> responses = invoiceService.getUnpaidInvoices();
        return ApiResponse.<List<InvoiceResponse>>builder()
                .message("Danh sách hóa đơn chưa thanh toán.")
                .result(responses)
                .build();
    }
}
