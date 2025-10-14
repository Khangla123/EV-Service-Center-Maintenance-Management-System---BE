package com.swp391.EV.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request cập nhật gói dịch vụ")
public class UpdateServicePackageRequest {

    @Schema(description = "Tên gói dịch vụ", example = "Bảo dưỡng nâng cao")
    private String name;

    @Schema(description = "Mô tả gói dịch vụ", example = "Gói bảo dưỡng nâng cao bao gồm kiểm tra toàn diện")
    private String description;

    @Positive(message = "Giá phải lớn hơn 0")
    @Schema(description = "Giá gói dịch vụ", example = "750000")
    private BigDecimal price;

    @Positive(message = "Thời gian thực hiện phải lớn hơn 0")
    @Schema(description = "Thời gian thực hiện (phút)", example = "90")
    private Integer durationMinutes;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
