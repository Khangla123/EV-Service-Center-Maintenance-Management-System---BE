package com.swp391.EV.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceHistoryFilterRequest {
    private UUID vehicleId;
    private LocalDate fromDate;
    private LocalDate toDate;
}
