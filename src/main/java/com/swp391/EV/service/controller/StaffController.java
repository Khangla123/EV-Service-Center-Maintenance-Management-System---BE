package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateStaffRequest;
import com.swp391.EV.service.dto.request.UpdateStaffProfileRequest;
import com.swp391.EV.service.dto.request.UpdateStaffRequest;
import com.swp391.EV.service.dto.response.StaffResponse;
import com.swp391.EV.service.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Staff Management APIs")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @Operation(summary = "Thêm nhân viên mới", description = "Create new staff member (Admin only)")
    public ApiResponse<StaffResponse> createStaff(@RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ApiResponse.<StaffResponse>builder()
                .message("Tạo nhân viên thành công.")
                .result(response)
                .build();
    }

    @GetMapping
    @Operation(summary = "Danh sách nhân viên", description = "Get all staff members (Admin only)")
    public ApiResponse<List<StaffResponse>> getAllStaff() {
        List<StaffResponse> responses = staffService.getAllStaff();
        return ApiResponse.<List<StaffResponse>>builder()
                .message("Danh sách nhân viên.")
                .result(responses)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết nhân viên", description = "Get staff details by ID (Admin only)")
    public ApiResponse<StaffResponse> getStaffById(@PathVariable UUID id) {
        StaffResponse response = staffService.getStaffById(id);
        return ApiResponse.<StaffResponse>builder()
                .message("Chi tiết nhân viên.")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật nhân viên", description = "Update staff information (Admin only)")
    public ApiResponse<StaffResponse> updateStaff(@PathVariable UUID id, @RequestBody UpdateStaffRequest request) {
        StaffResponse response = staffService.updateStaff(id, request);
        return ApiResponse.<StaffResponse>builder()
                .message("Cập nhật nhân viên thành công.")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa nhân viên", description = "Soft delete staff member (Admin only)")
    public ApiResponse<Void> deleteStaff(@PathVariable UUID id) {
        staffService.deleteStaff(id);
        return ApiResponse.<Void>builder()
                .message("Xóa nhân viên thành công.")
                .build();
    }

    @GetMapping("/available")
    @Operation(summary = "Nhân viên rảnh", description = "Get available technicians (Staff/Admin)")
    public ApiResponse<List<StaffResponse>> getAvailableStaff() {
        List<StaffResponse> responses = staffService.getAvailableStaff();
        return ApiResponse.<List<StaffResponse>>builder()
                .message("Danh sách nhân viên rảnh.")
                .result(responses)
                .build();
    }

    @GetMapping("/my-profile")
    @Operation(summary = "Hồ sơ của tôi", description = "Get own staff profile (Staff/Technician)")
    public ApiResponse<StaffResponse> getMyProfile() {
        StaffResponse response = staffService.getMyProfile();
        return ApiResponse.<StaffResponse>builder()
                .message("Thông tin hồ sơ của bạn.")
                .result(response)
                .build();
    }

    @PutMapping("/my-profile")
    @Operation(summary = "Cập nhật hồ sơ", description = "Update own staff profile (Staff/Technician)")
    public ApiResponse<StaffResponse> updateMyProfile(@RequestBody UpdateStaffProfileRequest request) {
        StaffResponse response = staffService.updateMyProfile(request);
        return ApiResponse.<StaffResponse>builder()
                .message("Cập nhật hồ sơ thành công.")
                .result(response)
                .build();
    }
}
