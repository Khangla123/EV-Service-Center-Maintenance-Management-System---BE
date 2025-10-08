package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateVehicleRequest;
import com.swp391.EV.service.dto.request.UpdateVehicleRequest;
import com.swp391.EV.service.dto.response.VehicleResponse;
import com.swp391.EV.service.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Quản lý phương tiện")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Tìm kiếm xe", description = "Lấy danh sách tất cả phương tiện")
    public ApiResponse<List<VehicleResponse>> getAllVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
        return ApiResponse.<List<VehicleResponse>>builder()
                .message("Danh sách phương tiện")
                .result(vehicles)
                .build();
    }

    @PostMapping
    @Operation(summary = "Thêm xe mới", description = "Đăng ký phương tiện mới")
    public ApiResponse<VehicleResponse> createVehicle(@RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Đăng ký phương tiện thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết xe", description = "Lấy thông tin chi tiết phương tiện theo ID")
    public ApiResponse<VehicleResponse> getVehicleById(@PathVariable UUID id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ApiResponse.<VehicleResponse>builder()
                .message("Chi tiết phương tiện")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin xe", description = "Cập nhật thông tin phương tiện")
    public ApiResponse<VehicleResponse> updateVehicle(@PathVariable UUID id,
                                                    @RequestBody UpdateVehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Cập nhật phương tiện thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa xe", description = "Xóa phương tiện (soft delete)")
    public ApiResponse<String> deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ApiResponse.<String>builder()
                .message("Xóa phương tiện thành công")
                .build();
    }

    @GetMapping("/customers/{customerId}")
    @Operation(summary = "Xe của khách hàng", description = "Lấy danh sách phương tiện của khách hàng")
    public ApiResponse<List<VehicleResponse>> getVehiclesByCustomer(@PathVariable UUID customerId) {
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return ApiResponse.<List<VehicleResponse>>builder()
                .message("Danh sách phương tiện của khách hàng")
                .result(vehicles)
                .build();
    }

    @PostMapping("/customers/{customerId}")
    @Operation(summary = "Thêm xe cho khách hàng", description = "Đăng ký phương tiện cho khách hàng")
    public ApiResponse<VehicleResponse> addVehicleForCustomer(@PathVariable UUID customerId,
                                                           @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicleForCustomer(customerId, request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Đăng ký phương tiện cho khách hàng thành công")
                .result(response)
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Xe của tôi", description = "Lấy danh sách phương tiện của người dùng hiện tại")
    public ApiResponse<List<VehicleResponse>> getMyVehicles(@RequestParam UUID customerId) {
        List<VehicleResponse> vehicles = vehicleService.getMyVehicles(customerId);
        return ApiResponse.<List<VehicleResponse>>builder()
                .message("Danh sách phương tiện của bạn")
                .result(vehicles)
                .build();
    }

    @PostMapping("/me")
    @Operation(summary = "Đăng ký xe mới", description = "Đăng ký phương tiện mới cho người dùng hiện tại")
    public ApiResponse<VehicleResponse> registerMyVehicle(@RequestParam UUID customerId,
                                                        @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicleForCustomer(customerId, request);
        return ApiResponse.<VehicleResponse>builder()
                .message("Đăng ký phương tiện thành công")
                .result(response)
                .build();
    }
}
