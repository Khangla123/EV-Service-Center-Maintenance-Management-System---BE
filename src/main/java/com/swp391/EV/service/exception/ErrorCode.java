package com.swp391.EV.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.OK),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED), // ko co quyen truy cap
    UNAUTHORIZED(1002, "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1003, "Invalid key", HttpStatus.BAD_REQUEST), // sai khoa
    USER_NOT_FOUND(1004, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND), // ko tim thay user
    USERNAME_OR_PASSWORD_ERROR(1005, "Sai email hoặc mật khẩu", HttpStatus.BAD_REQUEST),
    EMPTY_CREDENTIALS(1008, "Vui lòng nhập đầy đủ email và mật khẩu", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1006, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    ERROR_OTP(1007,"Sai otp" ,HttpStatus.BAD_REQUEST ),
    EXPIRY_OTP(1008,"Otp hết hạn" ,HttpStatus.BAD_REQUEST ),
    OTP_NOT_FOUND(1009,"Không tìm thấy otp" ,HttpStatus.NOT_FOUND ),
    OTP_NOT_VERIFY(1010,"Chưa xác thực đươc otp" ,HttpStatus.BAD_REQUEST );


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
