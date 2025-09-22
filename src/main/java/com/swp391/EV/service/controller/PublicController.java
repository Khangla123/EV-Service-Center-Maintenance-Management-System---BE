package com.swp391.EV.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "http://localhost:3000")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ Backend API is running successfully!");
    }
    
    @GetMapping("/status")
    public ResponseEntity<Object> getStatus() {
        return ResponseEntity.ok(new Object() {
            public final String status = "UP";
            public final String message = "EV Service Center API is running";
            public final String timestamp = java.time.LocalDateTime.now().toString();
        });
    }
}