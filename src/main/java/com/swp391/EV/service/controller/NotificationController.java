package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.response.NotificationResponse;
import com.swp391.EV.service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Quản lý thông báo cho customer")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my-notifications")
    @Operation(summary = "Lấy tất cả thông báo của tôi", 
               description = "Lấy danh sách tất cả thông báo của customer hiện tại")
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications();
        return ApiResponse.<List<NotificationResponse>>builder()
                .message("Danh sách thông báo")
                .result(notifications)
                .build();
    }

    @GetMapping("/unread")
    @Operation(summary = "Lấy thông báo chưa đọc", 
               description = "Lấy danh sách thông báo chưa đọc của customer hiện tại")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyUnreadNotifications();
        return ApiResponse.<List<NotificationResponse>>builder()
                .message("Danh sách thông báo chưa đọc")
                .result(notifications)
                .build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Đếm số thông báo chưa đọc", 
               description = "Lấy số lượng thông báo chưa đọc của customer hiện tại")
    public ApiResponse<Long> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ApiResponse.<Long>builder()
                .message("Số thông báo chưa đọc")
                .result(count)
                .build();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc", 
               description = "Đánh dấu một thông báo là đã đọc")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable UUID id) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ApiResponse.<NotificationResponse>builder()
                .message("Đã đánh dấu thông báo là đã đọc")
                .result(notification)
                .build();
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Đánh dấu tất cả đã đọc", 
               description = "Đánh dấu tất cả thông báo là đã đọc")
    public ApiResponse<String> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.<String>builder()
                .message("Đã đánh dấu tất cả thông báo là đã đọc")
                .build();
    }
}
