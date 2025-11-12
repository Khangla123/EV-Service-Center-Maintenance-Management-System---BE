package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.model.ServiceSuggestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSuggestionResponse {
    private UUID id;
    private String serviceName;
    private String reason;
    private BigDecimal estimatedCost;
    private ServiceSuggestion.SuggestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
