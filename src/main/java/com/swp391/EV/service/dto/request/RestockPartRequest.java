package com.swp391.EV.service.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockPartRequest {
    private Integer quantity;
}

