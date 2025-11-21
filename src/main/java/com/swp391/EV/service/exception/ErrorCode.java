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
    INVALID_ROLE(1018, "Vai trò không hợp lệ", HttpStatus.BAD_REQUEST),
    SERVICE_CENTER_NOT_FOUND(1019, "Không tìm thấy trung tâm dịch vụ", HttpStatus.NOT_FOUND),
    STAFF_NOT_FOUND(1020, "Không tìm thấy nhân viên", HttpStatus.NOT_FOUND),
    STAFF_ALREADY_EXISTS(1021, "Nhân viên đã tồn tại", HttpStatus.BAD_REQUEST),
    PART_NOT_FOUND(1022, "Không tìm thấy phụ tùng", HttpStatus.NOT_FOUND),
    PART_CODE_EXISTED(1023, "Mã phụ tùng đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1024, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    SERVICE_ORDER_NOT_FOUND(1025, "Không tìm thấy đơn dịch vụ", HttpStatus.NOT_FOUND),
    INVOICE_NOT_FOUND(1026, "Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(1027, "Không tìm thấy thanh toán", HttpStatus.NOT_FOUND),
    VEHICLE_NOT_FOUND(1028, "Không tìm thấy xe", HttpStatus.NOT_FOUND),
    APPOINTMENT_NOT_FOUND(1029, "Không tìm thấy lịch hẹn", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(1030, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
