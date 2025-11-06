package com.swp391.EV.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceHistoryStatisticsResponse {
    private Integer totalMaintenances;
    private BigDecimal totalCost;
    private BigDecimal averageCost;
    private List<MaintenanceHistoryResponse> maintenanceHistory;
}

