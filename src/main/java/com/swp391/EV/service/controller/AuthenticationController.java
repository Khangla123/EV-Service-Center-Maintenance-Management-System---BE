package com.swp391.EV.service.controller;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.LoginRequest;
import com.swp391.EV.service.dto.response.LoginResponse;
import com.swp391.EV.service.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) throws JOSEException {
        LoginResponse response = authenticationService.login(request.getEmail(), request.getPassword());
        return ApiResponse.<LoginResponse>builder()
                .message("Login thành công")
                .result(response)
                .build();
    }

}
