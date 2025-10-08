package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateServiceCenterRequest;
import com.swp391.EV.service.dto.request.UpdateServiceCenterRequest;
import com.swp391.EV.service.dto.response.ServiceCenterResponse;
import com.swp391.EV.service.service.ServiceCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-centers")
@RequiredArgsConstructor
@Tag(name = "Service Centers", description = "Quản lý trung tâm dịch vụ")
public class ServiceCenterController {

    private final ServiceCenterService serviceCenterService;

    @GetMapping
    @Operation(summary = "Danh sách trung tâm dịch vụ", description = "Lấy danh sách tất cả trung tâm dịch vụ")
    public ApiResponse<List<ServiceCenterResponse>> getAllServiceCenters(@RequestParam(required = false) String search) {
        List<ServiceCenterResponse> serviceCenters;
        if (search != null && !search.trim().isEmpty()) {
            serviceCenters = serviceCenterService.searchServiceCenters(search);
        } else {
            serviceCenters = serviceCenterService.getAllServiceCenters();
        }
        return ApiResponse.<List<ServiceCenterResponse>>builder()
                .message("Danh sách trung tâm dịch vụ")
                .result(serviceCenters)
                .build();
    }

    @PostMapping
    @Operation(summary = "Tạo trung tâm dịch vụ mới", description = "Thêm trung tâm dịch vụ mới vào hệ thống")
    public ApiResponse<ServiceCenterResponse> createServiceCenter(@RequestBody CreateServiceCenterRequest request) {
        ServiceCenterResponse response = serviceCenterService.createServiceCenter(request);
        return ApiResponse.<ServiceCenterResponse>builder()
                .message("Tạo trung tâm dịch vụ thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết trung tâm dịch vụ", description = "Lấy thông tin chi tiết trung tâm dịch vụ theo ID")
    public ApiResponse<ServiceCenterResponse> getServiceCenterById(@PathVariable UUID id) {
        ServiceCenterResponse response = serviceCenterService.getServiceCenterById(id);
        return ApiResponse.<ServiceCenterResponse>builder()
                .message("Chi tiết trung tâm dịch vụ")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật trung tâm dịch vụ", description = "Cập nhật thông tin trung tâm dịch vụ")
    public ApiResponse<ServiceCenterResponse> updateServiceCenter(@PathVariable UUID id,
                                                                @RequestBody UpdateServiceCenterRequest request) {
        ServiceCenterResponse response = serviceCenterService.updateServiceCenter(id, request);
        return ApiResponse.<ServiceCenterResponse>builder()
                .message("Cập nhật trung tâm dịch vụ thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa trung tâm dịch vụ", description = "Xóa trung tâm dịch vụ (soft delete)")
    public ApiResponse<String> deleteServiceCenter(@PathVariable UUID id) {
        serviceCenterService.deleteServiceCenter(id);
        return ApiResponse.<String>builder()
                .message("Xóa trung tâm dịch vụ thành công")
                .build();
    }
}
