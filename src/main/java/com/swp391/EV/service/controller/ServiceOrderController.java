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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
@Tag(name = "Service Orders", description = "Quản lý đơn dịch vụ")
public class ServiceOrderController {

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

    @PostMapping
    @Operation(summary = "Tạo đơn dịch vụ", description = "Tạo đơn dịch vụ mới từ lịch hẹn")
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
    @Operation(summary = "Phân công thợ", description = "Phân công kỹ thuật viên cho đơn dịch vụ")
    public ApiResponse<ServiceOrderResponse> assignTechnician(@PathVariable UUID id,
                                                            @RequestParam UUID technicianId) {
        ServiceOrderResponse response = serviceOrderService.assignTechnician(id, technicianId);
        return ApiResponse.<ServiceOrderResponse>builder()
                .message("Phân công kỹ thuật viên thành công")
                .result(response)
                .build();
    }

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

    @GetMapping("/my-assignments")
    @Operation(summary = "Công việc được giao", description = "Lấy danh sách công việc của kỹ thuật viên")
    public ApiResponse<List<ServiceOrderResponse>> getTechnicianTasks(@RequestParam UUID technicianId) {
        List<ServiceOrderResponse> tasks = serviceOrderService.getTechnicianTasks(technicianId);
        return ApiResponse.<List<ServiceOrderResponse>>builder()
                .message("Danh sách công việc của bạn")
                .result(tasks)
                .build();
    }
}
