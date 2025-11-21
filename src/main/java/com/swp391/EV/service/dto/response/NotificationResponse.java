package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private UUID id;
    private UUID customerId;
    private String customerName;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private UUID appointmentId;
    private UUID invoiceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
