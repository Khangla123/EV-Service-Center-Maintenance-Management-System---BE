package com.swp391.EV.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request tạo gói dịch vụ mới")
public class CreateServicePackageRequest {

    @NotBlank(message = "Tên gói dịch vụ không được để trống")
    @Schema(description = "Tên gói dịch vụ", example = "Bảo dưỡng cơ bản")
    private String name;

    @Schema(description = "Mô tả gói dịch vụ", example = "Gói bảo dưỡng cơ bản bao gồm kiểm tra pin, động cơ")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    @Schema(description = "Giá gói dịch vụ", example = "500000")
    private BigDecimal price;

    @Positive(message = "Thời gian thực hiện phải lớn hơn 0")
    @Schema(description = "Thời gian thực hiện (phút)", example = "60")
    private Integer durationMinutes;
}
