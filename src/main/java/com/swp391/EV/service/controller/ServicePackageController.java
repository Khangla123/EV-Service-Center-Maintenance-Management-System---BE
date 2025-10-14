package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateServicePackageRequest;
import com.swp391.EV.service.dto.request.UpdateServicePackageRequest;
import com.swp391.EV.service.dto.response.ServicePackageResponse;
import com.swp391.EV.service.service.ServicePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-packages")
@RequiredArgsConstructor
@Tag(name = "Service Packages", description = "Quản lý gói dịch vụ")
public class ServicePackageController {

    @Autowired
    private final ServicePackageService servicePackageService;

    @GetMapping
    @Operation(summary = "Danh sách gói dịch vụ", description = "Lấy danh sách tất cả gói dịch vụ")
    public ApiResponse<List<ServicePackageResponse>> getAllServicePackages() {
        List<ServicePackageResponse> servicePackages = servicePackageService.getAllServicePackages();
        return ApiResponse.<List<ServicePackageResponse>>builder()
                .message("Danh sách gói dịch vụ")
                .result(servicePackages)
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo gói dịch vụ", description = "Tạo gói dịch vụ mới (chỉ Admin)")
    public ApiResponse<ServicePackageResponse> createServicePackage(@Valid @RequestBody CreateServicePackageRequest request) {
        ServicePackageResponse response = servicePackageService.createServicePackage(request);
        return ApiResponse.<ServicePackageResponse>builder()
                .message("Tạo gói dịch vụ thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết gói dịch vụ", description = "Lấy thông tin chi tiết gói dịch vụ theo ID")
    public ApiResponse<ServicePackageResponse> getServicePackageById(@PathVariable UUID id) {
        ServicePackageResponse servicePackage = servicePackageService.getServicePackageById(id);
        return ApiResponse.<ServicePackageResponse>builder()
                .message("Chi tiết gói dịch vụ")
                .result(servicePackage)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật gói dịch vụ", description = "Cập nhật thông tin gói dịch vụ (chỉ Admin)")
    public ApiResponse<ServicePackageResponse> updateServicePackage(@PathVariable UUID id,
                                                                   @Valid @RequestBody UpdateServicePackageRequest request) {
        ServicePackageResponse response = servicePackageService.updateServicePackage(id, request);
        return ApiResponse.<ServicePackageResponse>builder()
                .message("Cập nhật gói dịch vụ thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa gói dịch vụ", description = "Xóa gói dịch vụ (soft delete - chỉ Admin)")
    public ApiResponse<String> deleteServicePackage(@PathVariable UUID id) {
        servicePackageService.deleteServicePackage(id);
        return ApiResponse.<String>builder()
                .message("Xóa gói dịch vụ thành công")
                .result("Gói dịch vụ đã được xóa")
                .build();
    }
}