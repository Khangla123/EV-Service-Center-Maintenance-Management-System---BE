package com.swp391.EV.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> { // tuong tu ResponseEntity trong spring
    @Builder.Default
    private int code = 1000; // mac dinh 100 la success
    private String message;
    private T result;
}
