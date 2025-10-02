package com.swp391.EV.service.service;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.request.RegisterRequest;
import com.swp391.EV.service.dto.response.GetAllUserResponse;
import com.swp391.EV.service.dto.response.LoginResponse;
import com.swp391.EV.service.dto.response.RegisterResponse;
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
                .role("user")
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
}
