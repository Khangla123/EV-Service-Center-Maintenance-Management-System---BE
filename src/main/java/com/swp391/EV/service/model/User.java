package com.swp391.EV.service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255, unique = true)
    private String email;

    @Column(nullable=false, length=255)
    private String password;

    @Column(name="first_name", length=255)
    private String firstName;

    @Column(name="last_name", length=255)
    private String lastName;

    @Column(length=20)
    private String phone;

    @Column(length=50, nullable=false)
    private String role = "CUSTOMER";

    @Column(length=500)
    private String avatar;

    @Column(name="is_active", nullable=false)
    private Boolean isActive = true;

    @Column(name="created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (role == null) role = "CUSTOMER";
        if (isActive == null) isActive = true;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) return null;
        return (firstName == null ? "" : firstName) + (lastName == null ? "" : (" " + lastName));
    }
}