package com.swp391.EV.service.service;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.request.RegisterRequest;
import com.swp391.EV.service.dto.request.UpdateUserRequest;
import com.swp391.EV.service.dto.response.GetAllUserResponse;
import com.swp391.EV.service.dto.response.LoginResponse;
import com.swp391.EV.service.dto.response.RegisterResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    public RegisterResponse register(RegisterRequest request) throws JOSEException {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank() ||
            request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(
                    ErrorCode.EMPTY_CREDENTIALS.getStatusCode(),
                    ErrorCode.EMPTY_CREDENTIALS.getMessage()
            );
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role("customer")
                .isActive(true)
                .createdAt(java.time.OffsetDateTime.now())
                .build();
        userRepository.save(user);

        String token = authService.generateTokenForUser(user);

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .build();

        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Đăng ký tài khoản thành công")
                .loginInfo(loginResponse)
                .build();
    }

    public List<GetAllUserResponse> getAllUser() {
        List<User> users = userRepository.findAll();
        List<GetAllUserResponse> responses = new ArrayList<>();
        for (User user : users) {
            GetAllUserResponse userResponse = GetAllUserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                    .build();
            responses.add(userResponse);
        }
        return responses;
    }

    public GetAllUserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return GetAllUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(String.valueOf(user.getCreatedAt()))
                .build();
    }

    public GetAllUserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole());
        }

        user.setUpdatedAt(java.time.OffsetDateTime.now());
        userRepository.save(user);

        return GetAllUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    public GetAllUserResponse updateRole(UUID id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate role
        if (role == null || role.isBlank()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Check if role is valid (admin, user, manager, etc.)
        if (!isValidRole(role)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        user.setRole(role);
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        userRepository.save(user);

        return GetAllUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

    private boolean isValidRole(String role) {
        return role.equals("admin") || role.equals("customer") || role.equals("technician") || role.equals("staff");
    }
}
