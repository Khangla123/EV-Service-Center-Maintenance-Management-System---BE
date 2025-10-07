package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // User fields embedded directly
    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "role")
    private String role = "CUSTOMER";

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "user_created_at")
    private OffsetDateTime userCreatedAt;

    @Column(name = "user_updated_at")
    private OffsetDateTime userUpdatedAt;

    // Customer specific fields
    @Column(name = "customer_code", unique = true)
    private String customerCode;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "subscription_expiry")
    private OffsetDateTime subscriptionExpiry;

    @Column(name = "total_spent", precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (userCreatedAt == null) {
            userCreatedAt = OffsetDateTime.now();
        }
        if (userUpdatedAt == null) {
            userUpdatedAt = OffsetDateTime.now();
        }
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        if (role == null) {
            role = "CUSTOMER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        userUpdatedAt = OffsetDateTime.now();
    }
}
