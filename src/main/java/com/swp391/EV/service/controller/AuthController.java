package com.swp391.EV.service.controller;

import com.nimbusds.jose.JOSEException;
import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.LoginRequest;
import com.swp391.EV.service.dto.request.RequestResetRequest;
import com.swp391.EV.service.dto.request.ResetPasswordRequest;
import com.swp391.EV.service.dto.request.VerifyOtpRequest;
import com.swp391.EV.service.dto.response.LoginResponse;
import com.swp391.EV.service.dto.response.MeResponse;
import com.swp391.EV.service.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) throws JOSEException {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ApiResponse.<LoginResponse>builder()
                .message("Login thành công")
                .result(response)
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> requestReset(@RequestBody RequestResetRequest request) {
        authService.requestPasswordRequest(request.getEmail());
        return ApiResponse.<Void>builder()
                .message("OTP đã được gửi qua mail")
                .result(null)
                .build();
    }

    @PostMapping("/verify-otp")
    ApiResponse<Void> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ApiResponse.<Void>builder()
                .message("Otp hợp lệ")
                .result(null)
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return ApiResponse.<Void>builder()
                .message("Mật khẩu đã được thay đổi")
                .result(null)
                .build();
    }

    @GetMapping("/me")
    ApiResponse<MeResponse> getUser(Authentication authentication){
        MeResponse response = authService.getMe(authentication);
        return ApiResponse.<MeResponse>builder()
                .message("Hiển thị thông tin user thành công")
                .result(response)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(Authentication authentication) {
        authService.logout(authentication);
        return ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .result(null)
                .build();
    }




}
