package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateVehicleRequest;
import com.swp391.EV.service.dto.request.CreateVehicleModelRequest;
import com.swp391.EV.service.dto.request.UpdateVehicleModelRequest;
import com.swp391.EV.service.dto.response.VehicleResponse;
import com.swp391.EV.service.dto.response.VehicleModelResponse;
import com.swp391.EV.service.service.VehicleService;
import com.swp391.EV.service.service.VehicleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Quản lý xe và loại xe")
public class VehicleController {

    @Autowired
    private final VehicleService vehicleService;
    @Autowired
    private final VehicleModelService vehicleModelService;


    @GetMapping
    @Operation(summary = "Tìm kiếm xe", description = "Lấy danh sách tất cả loại xe (STAFF/ADMIN) - Filter by customer/VIN")
    public ApiResponse<List<VehicleModelResponse>> getAllVehicles() {
        List<VehicleModelResponse> vehicles = vehicleModelService.getAllVehicleModels();
        return ApiResponse.<List<VehicleModelResponse>>builder()
                .message("Danh sách loại xe")
                .result(vehicles)
                .build();
    }

    @PostMapping
    @Operation(summary = "Thêm xe mới", description = "STAFF/ADMIN thêm loại xe mới vào hệ thống")
    public ApiResponse<VehicleModelResponse> createVehicle(@RequestBody CreateVehicleModelRequest request) {
        VehicleModelResponse response = vehicleModelService.createVehicleModel(request);
        return ApiResponse.<VehicleModelResponse>builder()
                .message("Thêm loại xe thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết xe", description = "Lấy thông tin chi tiết loại xe theo ID (STAFF/ADMIN/CUSTOMER)")
    public ApiResponse<VehicleModelResponse> getVehicleById(@PathVariable UUID id) {
        VehicleModelResponse response = vehicleModelService.getVehicleModelById(id);
        return ApiResponse.<VehicleModelResponse>builder()
                .message("Chi tiết loại xe")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin xe", description = "Cập nhật thông tin loại xe (STAFF/ADMIN)")
    public ApiResponse<VehicleModelResponse> updateVehicle(@PathVariable UUID id,
                                                    @RequestBody UpdateVehicleModelRequest request) {
        VehicleModelResponse response = vehicleModelService.updateVehicleModel(id, request);
        return ApiResponse.<VehicleModelResponse>builder()
                .message("Cập nhật loại xe thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa xe", description = "Xóa loại xe - soft delete (ADMIN)")
    public ApiResponse<String> deleteVehicle(@PathVariable UUID id) {
        vehicleModelService.deleteVehicleModel(id);
        return ApiResponse.<String>builder()
                .message("Xóa loại xe thành công")
                .build();
    }

    // ============= CUSTOMER VEHICLES (Xe của khách hàng) =============

    @GetMapping("/customers/{customerId}/vehicles")
    @Operation(summary = "Xe của khách hàng", description = "Lấy danh sách xe của khách hàng cụ thể (STAFF/ADMIN)")
    public ApiResponse<List<VehicleResponse>> getVehiclesByCustomer(@PathVariable UUID customerId) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ApiResponse.<List<VehicleResponse>>builder()
                .message("Danh sách phương tiện của khách hàng")
                .result(vehicles)
                .build();
    }

    @PostMapping("/customers/{customerId}/vehicles")
    @Operation(summary = "Thêm xe cho khách", description = "STAFF/ADMIN thêm xe cho khách hàng cụ thể")
    public ApiResponse<VehicleResponse> addVehicleForCustomer(
            @PathVariable UUID customerId,
            @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicleForCustomer(customerId, request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Đăng ký phương tiện cho khách hàng thành công")
                .result(response)
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Xe của tôi", description = "CUSTOMER xem danh sách xe của mình")
    public ApiResponse<List<VehicleResponse>> getMyVehicles(Authentication authentication) {
        // Lấy userId từ Authentication
        String userIdString = authentication != null ? authentication.getName() : null;
        UUID currentUserId = userIdString != null ? UUID.fromString(userIdString) : null;
        
        List<VehicleResponse> vehicles = vehicleService.getMyVehicles(currentUserId);
        
        return ApiResponse.<List<VehicleResponse>>builder()
                .message("Danh sách xe của bạn")
                .result(vehicles)
                .build();
    }

    @PostMapping("/me")
    @Operation(summary = "Đăng ký xe mới", description = "CUSTOMER tự đăng ký xe của mình")
    public ApiResponse<VehicleResponse> registerMyVehicle(
            @RequestBody CreateVehicleRequest request,
            Authentication authentication) {
        // Lấy userId từ Authentication
        String userIdString = authentication != null ? authentication.getName() : null;
        UUID currentUserId = userIdString != null ? UUID.fromString(userIdString) : null;
        
        VehicleResponse response = vehicleService.registerMyVehicle(currentUserId, request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Đăng ký xe thành công")
                .result(response)
                .build();
    }
}
