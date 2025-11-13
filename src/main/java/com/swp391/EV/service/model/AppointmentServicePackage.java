package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_service_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentServicePackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private ServiceAppointment appointment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_package_id", nullable = false)
    private ServicePackage servicePackage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Price at time of booking (for historical accuracy)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
