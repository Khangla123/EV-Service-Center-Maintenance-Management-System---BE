package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateServiceOrderRequest;
import com.swp391.EV.service.dto.request.UpdateServiceOrderRequest;
import com.swp391.EV.service.dto.response.ServiceOrderResponse;
import com.swp391.EV.service.model.ServiceOrder;
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
}

