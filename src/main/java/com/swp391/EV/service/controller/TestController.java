package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/hello")
    public ApiResponse<Map<String, String>> hello() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "Backend is working!");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ApiResponse.<Map<String, String>>builder()
                .code(1000)
                .message("Test successful")
                .result(result)
                .build();
    }

    @GetMapping("/maintenance-test")
    public ApiResponse<String> maintenanceTest() {
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Maintenance endpoint test")
                .result("MaintenanceHistoryController is loaded!")
                .build();
    }
    
    /**
     * TEMPORARY FIX: Drop old FK constraint on service_orders.technician_id
     * Call này CHỈ chạy 1 lần để fix DB, sau đó nên xóa endpoint này
     */
    @PostMapping("/fix-service-orders-fk")
    @Transactional
    public ApiResponse<String> fixServiceOrdersForeignKey() {
        try {
            // Drop old constraint (pointing to users.id)
            String dropConstraintSQL = "ALTER TABLE service_orders DROP CONSTRAINT IF EXISTS fkta3246v142v6vtiyjguub1vd2";
            entityManager.createNativeQuery(dropConstraintSQL).executeUpdate();
            
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Successfully dropped old FK constraint")
                    .result("Constraint 'fkta3246v142v6vtiyjguub1vd2' has been removed. service_orders.technician_id now only references staff.id")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .code(1001)
                    .message("Failed to fix FK constraint")
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * REFACTOR: Remove status column from service_orders
     * Status is now managed only in service_appointments.status
     */
    @PostMapping("/remove-service-order-status")
    @Transactional
    public ApiResponse<String> removeServiceOrderStatus() {
        try {
            String dropStatusSQL = "ALTER TABLE service_orders DROP COLUMN IF EXISTS status";
            entityManager.createNativeQuery(dropStatusSQL).executeUpdate();
            
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Successfully removed status column from service_orders")
                    .result("Status is now managed only in service_appointments.status (PENDING → CONFIRMED → ASSIGNED → IN_PROGRESS → COMPLETED/CANCELLED)")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .code(1001)
                    .message("Failed to remove status column")
                    .result("Error: " + e.getMessage())
                    .build();
        }
    }
}
