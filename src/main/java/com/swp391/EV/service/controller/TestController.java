package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

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
}
