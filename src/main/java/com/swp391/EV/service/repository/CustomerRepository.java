package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUsername(String username);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findCustomersWithSearch(@Param("search") String search, Pageable pageable);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
