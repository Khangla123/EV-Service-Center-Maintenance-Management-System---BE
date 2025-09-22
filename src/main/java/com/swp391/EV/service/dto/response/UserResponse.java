package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}