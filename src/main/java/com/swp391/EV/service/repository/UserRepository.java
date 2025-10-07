package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    // Methods for customer management
    Page<User> findByRole(String role, Pageable pageable);
    Page<User> findByRoleAndFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String role, String fullName, String email, Pageable pageable);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}