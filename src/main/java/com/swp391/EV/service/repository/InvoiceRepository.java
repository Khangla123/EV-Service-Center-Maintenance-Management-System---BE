package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerId(UUID customerId);

    List<Invoice> findByServiceOrderId(UUID serviceOrderId);
    
    // Fetch Invoice with all necessary relationships to avoid lazy loading
    @Query("SELECT i FROM Invoice i " +
           "LEFT JOIN FETCH i.serviceOrder so " +
           "LEFT JOIN FETCH so.appointment a " +
           "LEFT JOIN FETCH a.vehicle v " +
           "LEFT JOIN FETCH i.customer c " +
           "WHERE i.id = :id")
    Optional<Invoice> findByIdWithRelations(@Param("id") UUID id);
    
    // Fetch all Invoices with relationships to avoid lazy loading
    @Query("SELECT DISTINCT i FROM Invoice i " +
           "LEFT JOIN FETCH i.serviceOrder so " +
           "LEFT JOIN FETCH so.appointment a " +
           "LEFT JOIN FETCH a.vehicle v " +
           "LEFT JOIN FETCH i.customer c")
    List<Invoice> findAllWithRelations();
    
    // Fetch Invoices by customer ID with relationships
    @Query("SELECT i FROM Invoice i " +
           "LEFT JOIN FETCH i.serviceOrder so " +
           "LEFT JOIN FETCH so.appointment a " +
           "LEFT JOIN FETCH a.vehicle v " +
           "LEFT JOIN FETCH i.customer c " +
           "WHERE i.customer.id = :customerId")
    List<Invoice> findByCustomerIdWithRelations(@Param("customerId") UUID customerId);
}

