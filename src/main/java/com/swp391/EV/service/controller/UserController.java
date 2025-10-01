package com.swp391.EV.service.controller;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.RegisterRequest;
import com.swp391.EV.service.dto.response.RegisterResponse;
import com.swp391.EV.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
