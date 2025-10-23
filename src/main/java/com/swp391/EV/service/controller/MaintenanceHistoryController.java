package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.MaintenanceHistoryFilterRequest;
import com.swp391.EV.service.dto.response.MaintenanceHistoryStatisticsResponse;
import com.swp391.EV.service.service.MaintenanceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-history")
@RequiredArgsConstructor
@Tag(name = "Maintenance History", description = "APIs for customer maintenance history")
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceHistoryController {

    private final MaintenanceHistoryService maintenanceHistoryService;

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Test if controller is loaded")
    public ApiResponse<String> testEndpoint() {
        return ApiResponse.<String>builder()
                .code(1000)
                .message("MaintenanceHistoryController is working!")
                .result("Endpoint is accessible")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get maintenance history", description = "Get maintenance history for authenticated customer with optional filters")
    public ApiResponse<MaintenanceHistoryStatisticsResponse> getMaintenanceHistory(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        MaintenanceHistoryFilterRequest filter = MaintenanceHistoryFilterRequest.builder()
                .vehicleId(vehicleId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        MaintenanceHistoryStatisticsResponse response = maintenanceHistoryService.getMaintenanceHistory(filter);

        return ApiResponse.<MaintenanceHistoryStatisticsResponse>builder()
                .code(1000)
                .message("Get maintenance history successfully")
                .result(response)
                .build();
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Filter maintenance history", description = "Filter maintenance history using request body")
    public ApiResponse<MaintenanceHistoryStatisticsResponse> filterMaintenanceHistory(
            @RequestBody MaintenanceHistoryFilterRequest filter) {

        MaintenanceHistoryStatisticsResponse response = maintenanceHistoryService.getMaintenanceHistory(filter);

        return ApiResponse.<MaintenanceHistoryStatisticsResponse>builder()
                .code(1000)
                .message("Filter maintenance history successfully")
                .result(response)
                .build();
    }
}
