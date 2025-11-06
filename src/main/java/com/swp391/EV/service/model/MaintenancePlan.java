package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlan {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_model_id")
    private VehicleModel vehicleModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_package_id")
    private ServicePackage servicePackage;

    @Column(name = "interval_km")
    private Integer intervalKm;

    @Column(name = "interval_months")
    private Integer intervalMonths;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_template", columnDefinition = "jsonb")
    private String checklistTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "suggested_parts", columnDefinition = "jsonb")
    private String suggestedParts;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
