package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private ServiceAppointment appointment;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    // WORKAROUND: DB constraint yêu cầu technician_id references users(id)
    // Nhưng để dễ sử dụng trong code, ta map tới Staff và set technician_id thủ công
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", insertable = false, updatable = false)
    private Staff technician;  // Read-only, chỉ dùng để fetch data
    
    // Trường này sẽ lưu user.id vào database
    @Column(name = "technician_id")
    private UUID technicianUserId;

    // NOTE: Status được quản lý ở ServiceAppointment, không cần duplicate ở đây
    // ServiceOrder chỉ lưu thông tin chi tiết công việc (diagnosis, parts, cost...)

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String checklist; // JSON string

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;

    @Column(name = "total_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
