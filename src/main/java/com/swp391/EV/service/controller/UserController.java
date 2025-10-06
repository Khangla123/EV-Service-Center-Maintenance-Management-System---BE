package com.swp391.EV.service.controller;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.RegisterRequest;
import com.swp391.EV.service.dto.response.GetAllUserResponse;
import com.swp391.EV.service.dto.response.RegisterResponse;
import com.swp391.EV.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) throws JOSEException {
        RegisterResponse response = userService.register(request);
        return ApiResponse.<RegisterResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/user")
    ApiResponse<List<GetAllUserResponse>> getAllUser() {
        List<GetAllUserResponse> responses = userService.getAllUser();
        return ApiResponse.<List<GetAllUserResponse>>builder()
                .message("Danh sách user.")
                .result(responses)
                .build();
    }

    @GetMapping("/user/{id}")
    ApiResponse<GetAllUserResponse> getUserById(@PathVariable UUID id) {
        GetAllUserResponse response = userService.getUserById(id);
        return ApiResponse.<GetAllUserResponse>builder()
                .message("Thông tin user.")
                .result(response)
                .build();
    }

}
