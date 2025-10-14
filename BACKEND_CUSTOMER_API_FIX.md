# 🔧 Fix Backend: Endpoint /api/customers/me

## 🐛 Vấn đề

Backend endpoint `/api/customers/me` có thiết kế sai:
- ❌ Yêu cầu **@RequestParam UUID customerId** 
- ❌ Client phải biết customerId trước khi gọi API
- ❌ Không sử dụng JWT token để xác định user

**Đây là vấn đề thiết kế nghiêm trọng:**
- Customer cần gọi API để lấy customerId
- Nhưng API lại yêu cầu customerId làm input
- → Tạo ra vòng lặp không có điểm bắt đầu!

## ✅ Giải pháp

Sửa lại endpoint giống như VehicleController:
1. ✅ Nhận **Authentication** parameter từ Spring Security
2. ✅ Lấy **userId** từ JWT token (authentication.getName())
3. ✅ Tìm customer theo **userId** trong database
4. ✅ Trả về customer profile

## 📋 Các thay đổi

### 1. CustomerController.java

**Trước:**
```java
@GetMapping("/me")
ApiResponse<CustomerProfileResponse> getMyProfile(@RequestParam UUID customerId) {
    CustomerProfileResponse profile = customerService.getCustomerById(customerId);
    return ApiResponse.<CustomerProfileResponse>builder()
            .message("Hồ sơ của tôi")
            .result(profile)
            .build();
}
```

**Sau:**
```java
@GetMapping("/me")
ApiResponse<CustomerProfileResponse> getMyProfile(org.springframework.security.core.Authentication authentication) {
    System.out.println("👤 [CustomerController] /me endpoint called");
    System.out.println("🔑 [CustomerController] Authentication: " + (authentication != null ? "EXISTS" : "NULL"));
    
    if (authentication != null) {
        System.out.println("👤 [CustomerController] Authenticated: " + authentication.isAuthenticated());
        System.out.println("👤 [CustomerController] Principal (userId): " + authentication.getName());
    }
    
    // Lấy userId từ JWT token
    String userIdString = authentication != null ? authentication.getName() : null;
    UUID userId = userIdString != null ? UUID.fromString(userIdString) : null;
    
    System.out.println("🆔 [CustomerController] UserId from token: " + userId);
    
    // Tìm customer theo userId, KHÔNG PHẢI customerId
    CustomerProfileResponse profile = customerService.getCustomerByUserId(userId);
    
    System.out.println("✅ [CustomerController] Returning customer profile: " + profile.getFullName());
    
    return ApiResponse.<CustomerProfileResponse>builder()
            .message("Hồ sơ của tôi")
            .result(profile)
            .build();
}
```

### 2. CustomerService.java

**Thêm method mới:**
```java
public CustomerProfileResponse getCustomerByUserId(UUID userId) {
    System.out.println("👤 [CustomerService] Finding customer by userId: " + userId);
    
    Customer customer = customerRepository.findByUserId(userId)
            .orElseThrow(() -> {
                System.out.println("❌ [CustomerService] Customer not found for userId: " + userId);
                return new AppException(ErrorCode.USER_NOT_EXISTED);
            });
    
    System.out.println("✅ [CustomerService] Found customer: " + customer.getFullName() + " (customerId: " + customer.getId() + ")");
    
    return buildCustomerProfileResponse(customer);
}
```

### 3. CustomerRepository.java

**Đã có sẵn:**
```java
Optional<Customer> findByUserId(UUID userId);
```

## 🔍 Flow hoạt động

### Trước khi fix (❌ Sai):
```
Frontend → POST /api/customers/me?customerId=??? 
          ↑ Làm sao biết customerId???
```

### Sau khi fix (✅ Đúng):
```
1. User login → Backend trả về JWT token (chứa userId)
2. Frontend lưu token vào localStorage/header
3. Frontend gọi GET /api/customers/me (không cần parameter)
4. Backend:
   - Đọc JWT token từ Authorization header
   - Extract userId từ token: eab97d81-2ef2-4c4e-b85a-c61f77c1bd87
   - Query database: SELECT * FROM customers WHERE user_id = 'eab97d81-...'
   - Tìm thấy customer: {id: '6736a155-...', fullName: 'khang', ...}
5. Frontend nhận customerId từ response
```

## 📊 Database Schema

```
┌─────────────────────────────────────────┐
│          customers table                │
├─────────────────────────────────────────┤
│ id (customerId)      | UUID (PK)        │ ← Đây là customerId
│ user_id             | UUID (UNIQUE)     │ ← Đây là userId từ JWT token
│ full_name           | VARCHAR           │
│ email               | VARCHAR           │
│ phone               | VARCHAR           │
│ address             | TEXT              │
│ customer_code       | VARCHAR           │
│ ...                                     │
└─────────────────────────────────────────┘

Query: SELECT * FROM customers WHERE user_id = ?
       ↑ Tìm theo userId từ JWT token
```

## 🎯 Testing

### Bước 1: Rebuild Backend

Trong IntelliJ IDEA:
1. Stop application (Shift + F5)
2. Build → Rebuild Project (Ctrl + F9)
3. Run 'EvServiceApplication' (Shift + F10)
4. Đợi log: "Tomcat started on port(s): 8080"

### Bước 2: Test API với Postman/cURL

**Request:**
```bash
GET http://localhost:8080/api/customers/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Backend Logs (Success):**
```
👤 [CustomerController] /me endpoint called
🔑 [CustomerController] Authentication: EXISTS
👤 [CustomerController] Authenticated: true
👤 [CustomerController] Principal (userId): eab97d81-2ef2-4c4e-b85a-c61f77c1bd87
🆔 [CustomerController] UserId from token: eab97d81-2ef2-4c4e-b85a-c61f77c1bd87
👤 [CustomerService] Finding customer by userId: eab97d81-2ef2-4c4e-b85a-c61f77c1bd87
✅ [CustomerService] Found customer: khang (customerId: 6736a155-b5a3-43b0-94c6-9313cd000ff2)
✅ [CustomerController] Returning customer profile: khang
```

**Response:**
```json
{
  "message": "Hồ sơ của tôi",
  "result": {
    "id": "6736a155-b5a3-43b0-94c6-9313cd000ff2",
    "userId": "eab97d81-2ef2-4c4e-b85a-c61f77c1bd87",
    "fullName": "khang",
    "email": "khang@gmail.com",
    "phone": "0123456789",
    "address": "Hà Nội",
    "customerCode": "CUS001",
    ...
  }
}
```

### Bước 3: Hard Refresh Frontend

```
Ctrl + Shift + R
```

### Bước 4: Test trang Booking

Frontend sẽ gọi:
```typescript
const customer = await customerService.getMyProfile();
// Response: {id: "6736a155-...", userId: "eab97d81-...", ...}

setCustomerId(customer.id); // Lưu customerId
```

Khi submit appointment:
```typescript
const createRequest = {
  customerId: customerId, // ✅ 6736a155-b5a3-43b0-94c6-9313cd000ff2
  vehicleId: selectedVehicle.id,
  ...
};
```

Backend logs:
```
👤 [AppointmentService] Finding customer: 6736a155-b5a3-43b0-94c6-9313cd000ff2
✅ [AppointmentService] Found customer: khang  ← ✅ Thành công!
```

## 📝 Tóm tắt

### Vấn đề gốc:
- Frontend không biết customerId
- Backend `/api/customers/me` yêu cầu customerId làm input
- → Không thể lấy được customerId!

### Giải pháp:
- ✅ Backend sửa endpoint `/api/customers/me` lấy userId từ JWT token
- ✅ Backend tìm customer theo userId → trả về customerId
- ✅ Frontend lưu customerId và sử dụng cho các API khác

### Key Changes:
1. ✅ CustomerController.getMyProfile() nhận Authentication parameter
2. ✅ Extract userId từ JWT token
3. ✅ CustomerService.getCustomerByUserId() tìm theo userId
4. ✅ Repository đã có findByUserId() method

### Restart Backend để apply changes! 🚀
