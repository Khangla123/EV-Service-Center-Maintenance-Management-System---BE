package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a customer, ordered by created date (newest first)
     */
    @Query("SELECT n FROM Notification n WHERE n.customer.id = :customerId ORDER BY n.createdAt DESC")
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId);
    
    /**
     * Find unread notifications for a customer
     */
    @Query("SELECT n FROM Notification n WHERE n.customer.id = :customerId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Count unread notifications for a customer
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customer.id = :customerId AND n.isRead = false")
    long countUnreadByCustomerId(@Param("customerId") UUID customerId);
}
