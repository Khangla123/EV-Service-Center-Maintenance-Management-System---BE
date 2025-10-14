package com.swp391.EV.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Response thông tin gói dịch vụ")
public class ServicePackageResponse {

    @Schema(description = "ID gói dịch vụ")
    private UUID id;

    @Schema(description = "Tên gói dịch vụ")
    private String name;

    @Schema(description = "Mô tả gói dịch vụ")
    private String description;

    @Schema(description = "Giá gói dịch vụ")
    private BigDecimal price;

    @Schema(description = "Thời gian thực hiện (phút)")
    private Integer durationMinutes;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean isActive;

    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;
}
