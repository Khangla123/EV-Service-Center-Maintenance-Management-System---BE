package com.swp391.EV.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED), // ko co quyen truy cap
    UNAUTHORIZED(1002, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_KEY(1003, "Invalid key", HttpStatus.BAD_REQUEST), // sai khoa
    USER_NOT_EXISTED(1004, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND), // ko tim thay user
    USER_NOT_FOUND(1004, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND), // ko tim thay user (alias)
    USERNAME_OR_PASSWORD_ERROR(1005, "Sai email hoặc mật khẩu", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1006, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1006, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST), // alias for USER_EXISTED
    ERROR_OTP(1007,"Sai otp", HttpStatus.BAD_REQUEST),
    EMPTY_CREDENTIALS(1008, "Vui lòng nhập đầy đủ email và mật khẩu", HttpStatus.BAD_REQUEST),
    EXPIRY_OTP(1009,"Otp hết hạn", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1010,"Không tìm thấy otp", HttpStatus.NOT_FOUND),
    OTP_NOT_VERIFY(1011,"Chưa xác thực được otp", HttpStatus.BAD_REQUEST),
    CUSTOMER_NOT_FOUND(1012, "Không tìm thấy khách hàng", HttpStatus.NOT_FOUND),
    INVALID_CUSTOMER_DATA(1013, "Dữ liệu khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    CUSTOMER_ALREADY_EXISTS(1014, "Khách hàng đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1015, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ACTION(1016, "Không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    DATABASE_ERROR(1017, "Lỗi cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ROLE(1018, "Vai trò không hợp lệ", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
