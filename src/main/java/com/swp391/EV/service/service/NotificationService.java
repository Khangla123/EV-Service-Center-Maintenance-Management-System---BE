package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.response.NotificationResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.Notification;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    /**
     * Create a notification for appointment created by staff
     */
    @Transactional
    public Notification createAppointmentNotification(UUID customerId, UUID appointmentId, String appointmentDetails) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(Notification.NotificationType.APPOINTMENT_CREATED_BY_STAFF)
                .title("Nhắc nhở bảo dưỡng")
                .message("Bạn có 2 xe cần bảo dưỡng. " + appointmentDetails)
                .appointmentId(appointmentId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Create a notification for invoice created
     */
    @Transactional
    public Notification createInvoiceNotification(UUID customerId, UUID invoiceId, String invoiceDetails) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(Notification.NotificationType.INVOICE_CREATED)
                .title("Nhắc nhở thanh toán")
                .message("Bạn có 2 khoản cần thanh toán. " + invoiceDetails)
                .invoiceId(invoiceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for current logged-in customer
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for current logged-in customer
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnreadNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findUnreadByCustomerId(customer.getId());
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count for current logged-in customer
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return notificationRepository.countUnreadByCustomerId(customer.getId());
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // Verify that the notification belongs to the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (!notification.getCustomer().getId().equals(customer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);
        
        return mapToResponse(updatedNotification);
    }

    /**
     * Mark all notifications as read for current user
     */
    @Transactional
    public void markAllAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<Notification> unreadNotifications = notificationRepository.findUnreadByCustomerId(customer.getId());
        
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Map Notification entity to NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer().getId())
                .customerName(notification.getCustomer().getFullName())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .appointmentId(notification.getAppointmentId())
                .invoiceId(notification.getInvoiceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
