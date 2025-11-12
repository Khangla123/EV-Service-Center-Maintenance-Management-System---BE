package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateServiceOrderRequest;
import com.swp391.EV.service.dto.request.UpdateServiceOrderRequest;
import com.swp391.EV.service.dto.response.ServiceOrderResponse;
import com.swp391.EV.service.service.ServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
@Tag(name = "Service Orders", description = "Quản lý đơn dịch vụ")
public class ServiceOrderController {

    @Autowired
    private final ServiceOrderService serviceOrderService;

    @GetMapping
    @Operation(summary = "Danh sách đơn dịch vụ", description = "Lấy danh sách tất cả đơn dịch vụ")
    public ApiResponse<List<ServiceOrderResponse>> getAllServiceOrders() {
        List<ServiceOrderResponse> orders = serviceOrderService.getAllServiceOrders();
        return ApiResponse.<List<ServiceOrderResponse>>builder()
                .message("Danh sách đơn dịch vụ")
                .result(orders)
                .build();
    }

    /**
     * ENDPOINT DÀNH CHO STAFF - Tạo đơn dịch vụ trực tiếp
     * Staff có thể tạo service order trực tiếp mà không cần qua appointment
     */
    @PostMapping
    @Operation(summary = "Tạo đơn dịch vụ trực tiếp [STAFF]",
               description = "Staff tạo đơn dịch vụ trực tiếp từ appointment. " +
                            "Dùng khi staff muốn tạo order thủ công, không qua flow confirm.")
    public ApiResponse<ServiceOrderResponse> createServiceOrder(@RequestBody CreateServiceOrderRequest request) {
        ServiceOrderResponse response = serviceOrderService.createServiceOrder(request);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Tạo đơn dịch vụ thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn dịch vụ", description = "Lấy thông tin chi tiết đơn dịch vụ")
    public ApiResponse<ServiceOrderResponse> getServiceOrderById(@PathVariable UUID id) {
        ServiceOrderResponse response = serviceOrderService.getServiceOrderById(id);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Chi tiết đơn dịch vụ")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật đơn dịch vụ", description = "Cập nhật thông tin đơn dịch vụ")
    public ApiResponse<ServiceOrderResponse> updateServiceOrder(@PathVariable UUID id,
                                                              @RequestBody UpdateServiceOrderRequest request) {
        ServiceOrderResponse response = serviceOrderService.updateServiceOrder(id, request);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Cập nhật đơn dịch vụ thành công")
                .result(response)
                .build();
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Phân công lại thợ",
               description = "Phân công lại kỹ thuật viên cho đơn dịch vụ ĐÃ TỒN TẠI (không tạo mới)")
    public ApiResponse<ServiceOrderResponse> assignTechnician(@PathVariable UUID id,
                                                            @RequestParam UUID technicianId) {
        ServiceOrderResponse response = serviceOrderService.assignTechnician(id, technicianId);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Phân công kỹ thuật viên thành công")
                .result(response)
                .build();
    }

    // NOTE: Status management removed from ServiceOrder
    // Use AppointmentController.updateStatus() to update appointment.status instead
    /*
    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái", description = "Cập nhật trạng thái đơn dịch vụ")
    public ApiResponse<ServiceOrderResponse> updateStatus(@PathVariable UUID id,
                                                        @RequestParam ServiceOrder.ServiceStatus status) {
        ServiceOrderResponse response = serviceOrderService.updateStatus(id, status);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Cập nhật trạng thái thành công")
                .result(response)
                .build();
    }
    */

    @GetMapping("/my-assignments")
    @Operation(summary = "Công việc được giao", description = "Lấy danh sách công việc của kỹ thuật viên")
    public ApiResponse<List<ServiceOrderResponse>> getTechnicianTasks(@RequestParam UUID technicianId) {
        List<ServiceOrderResponse> tasks = serviceOrderService.getTechnicianTasks(technicianId);
        return ApiResponse.<List<ServiceOrderResponse>>builder()
                .message("Danh sách công việc của bạn")
                .result(tasks)
                .build();
    }

    /**
     * ENDPOINT THEO FLOW CHUẨN - Từ Appointment đã CONFIRMED
     * Flow: Customer đặt lịch → Staff xác nhận → Phân công thợ (endpoint này) → Tạo Service Order
     */
    @PostMapping("/from-appointment/{appointmentId}/assign")
    @Operation(summary = "Phân công thợ và tạo đơn dịch vụ [FLOW CHUẨN]",
               description = "Từ lịch hẹn đã CONFIRMED của customer, phân công kỹ thuật viên và tạo đơn dịch vụ mới. " +
                            "Flow: Customer đặt lịch → Staff confirm → Staff phân công thợ (API này) → Tạo Service Order")
    public ApiResponse<ServiceOrderResponse> createServiceOrderAndAssign(
            @PathVariable UUID appointmentId,
            @RequestParam UUID technicianId) {
        System.out.println("=== CONTROLLER DEBUG ===");
        System.out.println("Received appointmentId: " + appointmentId);
        System.out.println("Received technicianId: " + technicianId);
        System.out.println("========================");
        
        ServiceOrderResponse response = serviceOrderService.createServiceOrderFromAppointment(appointmentId, technicianId);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Phân công kỹ thuật viên và tạo đơn dịch vụ thành công")
                .result(response)
                .build();
    }

    /**
     * ENDPOINT CHO TECHNICIAN - Lấy service orders có checklist
     */
    @GetMapping("/technician/me")
    @Operation(summary = "Lấy service orders của technician [TECHNICIAN]",
               description = "Technician lấy danh sách service orders được gán kèm theo checklist từ maintenance_plans")
    public ApiResponse<List<ServiceOrderResponse>> getMyServiceOrders() {
        // TODO: Get current user from JWT token
        // For now, return all service orders
        List<ServiceOrderResponse> orders = serviceOrderService.getAllServiceOrders();
        return ApiResponse.<List<ServiceOrderResponse>>builder()
                .message("Danh sách công việc của bạn")
                .result(orders)
                .build();
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Lấy service order theo appointment ID",
               description = "Lấy service order dựa trên appointment ID để hiển thị checklist")
    public ApiResponse<ServiceOrderResponse> getServiceOrderByAppointmentId(@PathVariable UUID appointmentId) {
        ServiceOrderResponse response = serviceOrderService.getServiceOrderByAppointmentId(appointmentId);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Service order của appointment")
                .result(response)
                .build();
    }

    @PutMapping("/{id}/issues")
    @Operation(summary = "Cập nhật vấn đề phát hiện [TECHNICIAN]",
               description = "Technician cập nhật danh sách vấn đề phát hiện trong quá trình kiểm tra. " +
                            "Format: JSON array [{issue, severity, recommendation}]")
    public ApiResponse<ServiceOrderResponse> updateIssues(
            @PathVariable UUID id,
            @RequestBody String issuesJson) {
        ServiceOrderResponse response = serviceOrderService.updateIssues(id, issuesJson);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Cập nhật vấn đề phát hiện thành công")
                .result(response)
                .build();
    }

    @PutMapping("/{id}/issues/set-price")
    @Operation(summary = "Set giá cho issue [STAFF/ADMIN]",
               description = "Staff/Admin set giá cho các vấn đề phát hiện. " +
                            "Format: JSON {issueId: 'ISS-123', price: 500000}")
    public ApiResponse<ServiceOrderResponse> setIssuePrice(
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, Object> requestBody) {
        String issueId = (String) requestBody.get("issueId");
        Number priceNumber = (Number) requestBody.get("price");
        Double price = priceNumber != null ? priceNumber.doubleValue() : null;
        
        ServiceOrderResponse response = serviceOrderService.setIssuePrice(id, issueId, price);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Cập nhật giá thành công")
                .result(response)
                .build();
    }

    @PostMapping("/{id}/parts")
    @Operation(summary = "Thêm phụ tùng đã sử dụng [TECHNICIAN]",
               description = "Technician thêm phụ tùng đã sử dụng trong quá trình bảo dưỡng. " +
                            "Format: JSON array [{partCode, partName, quantity, unit}]")
    public ApiResponse<ServiceOrderResponse> addPartsUsed(
            @PathVariable UUID id,
            @RequestBody String partsJson) {
        ServiceOrderResponse response = serviceOrderService.addPartsUsed(id, partsJson);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Thêm phụ tùng thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}/parts")
    @Operation(summary = "Lấy danh sách phụ tùng đã sử dụng",
               description = "Lấy chi tiết tất cả phụ tùng đã sử dụng cho service order")
    public ApiResponse<java.util.List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse>> getPartsUsed(@PathVariable UUID id) {
        java.util.List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse> parts = serviceOrderService.getPartsUsed(id);
        return ApiResponse.<java.util.List<com.swp391.EV.service.dto.response.ServiceOrderPartResponse>>builder()
                .message("Danh sách phụ tùng đã sử dụng")
                .result(parts)
                .build();
    }

    @GetMapping("/{id}/parts/count")
    @Operation(summary = "Đếm phụ tùng đã sử dụng",
               description = "Đếm số lượng loại phụ tùng đã sử dụng cho service order")
    public ApiResponse<Integer> getPartsUsedCount(@PathVariable UUID id) {
        int count = serviceOrderService.getPartsUsedCount(id);
        return ApiResponse.<Integer>builder()
                .message("Số loại phụ tùng đã sử dụng")
                .result(count)
                .build();
    }

    @GetMapping("/{id}/parts/summary")
    @Operation(summary = "Lấy tóm tắt phụ tùng (count + total)",
               description = "Lấy số lượng và tổng tiền phụ tùng đã sử dụng")
    public ApiResponse<java.util.Map<String, Object>> getPartsUsedSummary(@PathVariable UUID id) {
        java.util.Map<String, Object> summary = serviceOrderService.getPartsUsedSummary(id);
        return ApiResponse.<java.util.Map<String, Object>>builder()
                .message("Tóm tắt phụ tùng đã sử dụng")
                .result(summary)
                .build();
    }

    @PostMapping("/{id}/suggestions")
    @Operation(summary = "Thêm đề xuất dịch vụ [TECHNICIAN]",
               description = "Technician đề xuất dịch vụ bổ sung khi phát hiện vấn đề khác. " +
                            "Format: JSON {serviceName, reason, estimatedCost}")
    public ApiResponse<ServiceOrderResponse> addServiceSuggestion(
            @PathVariable UUID id,
            @RequestBody String suggestionJson) {
        ServiceOrderResponse response = serviceOrderService.addServiceSuggestion(id, suggestionJson);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Thêm đề xuất dịch vụ thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}/suggestions")
    @Operation(summary = "Lấy danh sách đề xuất dịch vụ",
               description = "Lấy tất cả đề xuất dịch vụ của service order (return DTO)")
    public ApiResponse<List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse>> getServiceSuggestions(
            @PathVariable UUID id) {
        List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse> suggestions = serviceOrderService.getServiceSuggestions(id);
        return ApiResponse.<List<com.swp391.EV.service.dto.response.ServiceSuggestionResponse>>builder()
                .message("Danh sách đề xuất dịch vụ")
                .result(suggestions)
                .build();
    }

    @PutMapping("/suggestions/{suggestionId}/status")
    @Operation(summary = "Cập nhật trạng thái đề xuất [STAFF/CUSTOMER]",
               description = "Chấp nhận hoặc từ chối đề xuất dịch vụ. Status: APPROVED/REJECTED")
    public ApiResponse<com.swp391.EV.service.model.ServiceSuggestion> updateSuggestionStatus(
            @PathVariable UUID suggestionId,
            @RequestParam com.swp391.EV.service.model.ServiceSuggestion.SuggestionStatus status) {
        com.swp391.EV.service.model.ServiceSuggestion suggestion = serviceOrderService.updateSuggestionStatus(suggestionId, status);
        return ApiResponse.<com.swp391.EV.service.model.ServiceSuggestion>builder()
                .message("Cập nhật trạng thái đề xuất thành công")
                .result(suggestion)
                .build();
    }
}

