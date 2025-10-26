package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreateAppointmentRequest;
import com.swp391.EV.service.dto.request.UpdateAppointmentRequest;
import com.swp391.EV.service.dto.response.AppointmentResponse;
import com.swp391.EV.service.model.ServiceAppointment;
import com.swp391.EV.service.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Quản lý lịch hẹn")
public class AppointmentController {

    @Autowired
    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Danh sách lịch hẹn", description = "Lấy danh sách tất cả lịch hẹn")
    public ApiResponse<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ApiResponse.<List<AppointmentResponse>>builder()
                .message("Danh sách lịch hẹn")
                .result(appointments)
                .build();
    }

    @PostMapping
    @Operation(summary = "Đặt lịch hẹn", description = "Tạo lịch hẹn mới")
    public ApiResponse<AppointmentResponse> createAppointment(@RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        
        return ApiResponse.<AppointmentResponse>builder()
                .message("Đặt lịch hẹn thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lịch hẹn", description = "Lấy thông tin chi tiết lịch hẹn theo ID")
    public ApiResponse<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ApiResponse.<AppointmentResponse>builder()
                .message("Chi tiết lịch hẹn")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lịch hẹn", description = "Cập nhật thông tin lịch hẹn")
    public ApiResponse<AppointmentResponse> updateAppointment(@PathVariable UUID id,
                                                            @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ApiResponse.<AppointmentResponse>builder()
                .message("Cập nhật lịch hẹn thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy lịch hẹn", description = "Hủy lịch hẹn theo ID")
    public ApiResponse<String> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ApiResponse.<String>builder()
                .message("Hủy lịch hẹn thành công")
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Lịch hẹn của tôi", description = "Lấy danh sách lịch hẹn của khách hàng hiện tại")
    public ApiResponse<List<AppointmentResponse>> getMyAppointments(
            @RequestParam(required = false) UUID customerId) {
        // If customerId not provided, return empty list for now
        // TODO: Get customerId from JWT token in SecurityContext
        if (customerId == null) {
            return ApiResponse.<List<AppointmentResponse>>builder()
                    .message("Customer ID is required")
                    .result(List.of())
                    .build();
        }
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByCustomerId(customerId);
        return ApiResponse.<List<AppointmentResponse>>builder()
                .message("Danh sách lịch hẹn của bạn")
                .result(appointments)
                .build();
    }

    @GetMapping("/available")
    @Operation(summary = "Khung giờ trống", description = "Lấy danh sách khung giờ có thể đặt lịch")
    public ApiResponse<List<AppointmentResponse>> getAvailableTimeSlots() {
        List<AppointmentResponse> timeSlots = appointmentService.getAvailableTimeSlots();
        return ApiResponse.<List<AppointmentResponse>>builder()
                .message("Danh sách khung giờ trống")
                .result(timeSlots)
                .build();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Xác nhận lịch hẹn",
               description = "Staff xác nhận lịch hẹn (PENDING -> CONFIRMED)")
    public ApiResponse<AppointmentResponse> confirmAppointment(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.confirmAppointment(id);
        return ApiResponse.<AppointmentResponse>builder()
                .message("Xác nhận lịch hẹn thành công")
                .result(response)
                .build();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy lịch hẹn",
               description = "Staff hủy lịch hẹn với lý do")
    public ApiResponse<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, reason);
        return ApiResponse.<AppointmentResponse>builder()
                .message("Hủy lịch hẹn thành công")
                .result(response)
                .build();
    }

    @GetMapping("/by-status")
    @Operation(summary = "Lọc lịch hẹn theo trạng thái",
               description = "Lấy danh sách lịch hẹn theo trạng thái (PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED)")
    public ApiResponse<List<AppointmentResponse>> getAppointmentsByStatus(
            @RequestParam ServiceAppointment.AppointmentStatus status) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(status);
        return ApiResponse.<List<AppointmentResponse>>builder()
                .message("Danh sách lịch hẹn " + status)
                .result(appointments)
                .build();
    }

    @GetMapping("/my-tasks")
    @Operation(summary = "Công việc của tôi", 
               description = "Lấy danh sách công việc được phân công cho technician hiện tại")
    public ApiResponse<List<AppointmentResponse>> getMyTasks(@RequestParam UUID technicianId) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByTechnicianId(technicianId);
        return ApiResponse.<List<AppointmentResponse>>builder()
                .message("Danh sách công việc của bạn")
                .result(appointments)
                .build();
    }
}
