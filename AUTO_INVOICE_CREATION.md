# Tự động tạo hóa đơn khi hoàn thành kiểm tra

## Tổng quan
Hệ thống đã được cập nhật để **tự động tạo hóa đơn thanh toán** khi kỹ thuật viên hoàn thành công việc kiểm tra (chuyển trạng thái appointment sang `COMPLETED`).

## Các thay đổi chính

### 1. Backend Changes

#### 1.1. Invoice Model (`Invoice.java`)
- **Thêm field `status`** để theo dõi trạng thái thanh toán
- Enum `InvoiceStatus`: `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`
- Mặc định: `PENDING`

```java
@Enumerated(EnumType.STRING)
@Column(name = "status", length = 20)
@Builder.Default
private InvoiceStatus status = InvoiceStatus.PENDING;
```

#### 1.2. InvoiceResponse DTO
- Thêm các field mới:
  - `status`: Trạng thái hóa đơn
  - `vehicleLicensePlate`: Biển số xe
  - `finalAmount`: Số tiền cuối cùng (alias của totalAmount)
  - `issueDate`: Ngày phát hành (alias của issuedAt)
  - `discount`: Giảm giá (alias của discountAmount)
  - `notes`: Ghi chú

#### 1.3. AppointmentService
- **Inject `InvoiceService`** để tự động tạo invoice
- **Thêm logic tự động tạo invoice** khi appointment chuyển sang `COMPLETED`:

```java
if (newStatus == ServiceAppointment.AppointmentStatus.COMPLETED 
    && appointment.getActualCompletion() == null) {
    appointment.setActualCompletion(LocalDateTime.now());
    
    // TỰ ĐỘNG TẠO INVOICE
    try {
        createInvoiceForCompletedAppointment(appointment);
    } catch (Exception e) {
        System.err.println("Failed to auto-create invoice: " + e.getMessage());
    }
}
```

- **Method mới `createInvoiceForCompletedAppointment()`**:
  1. Kiểm tra xem đã có ServiceOrder chưa
  2. Kiểm tra xem đã có Invoice chưa (tránh tạo trùng)
  3. Tính toán chi phí từ ServicePackage
  4. Tính thuế 10%
  5. Tạo invoice với hạn thanh toán 7 ngày

#### 1.4. InvoiceService
- **Method mới `getInvoiceByServiceOrderId(UUID)`**: Kiểm tra invoice đã tồn tại
- **Method mới `getUnpaidInvoices()`**: Lấy danh sách hóa đơn chưa thanh toán
- **Cập nhật `mapToResponse()`**: Map đầy đủ các field bao gồm status, vehicle info, etc.

#### 1.5. InvoiceController
- **Endpoint mới `GET /api/invoices/unpaid`**: Lấy danh sách hóa đơn chưa thanh toán

### 2. Database Migration

File: `database/add_invoice_status.sql`

```sql
-- Add status column to invoices table
ALTER TABLE invoices 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';

-- Update existing invoices to PENDING status
UPDATE invoices 
SET status = 'PENDING' 
WHERE status IS NULL;
```

**Chạy migration:**
```bash
psql -U postgres -d EVService -f database/add_invoice_status.sql
```

### 3. Frontend Integration

Frontend đã sẵn sàng với:
- `invoiceService.getUnpaidInvoices()`: Gọi API `/api/invoices/unpaid`
- OnlinePayment component hiển thị danh sách hóa đơn chưa thanh toán
- Nút "Thanh toán" chỉ hiện khi `serviceStatus === 'COMPLETED'`

## Workflow hoàn chỉnh

```
1. Customer đặt lịch → Appointment (PENDING)
   ↓
2. Staff xác nhận → Appointment (CONFIRMED)
   ↓
3. Staff tạo ServiceOrder và gán Technician → Appointment (IN_PROGRESS)
   ↓
4. Technician hoàn thành công việc → Appointment (COMPLETED)
   ↓
5. HỆ THỐNG TỰ ĐỘNG:
   - Tạo Invoice với status = PENDING
   - Tính chi phí từ ServicePackage
   - Thêm thuế 10%
   - Set hạn thanh toán 7 ngày
   ↓
6. Customer vào trang "Quản lý chi phí":
   - Thấy nút "Thanh toán" với dịch vụ đã COMPLETED
   - Click → Chuyển đến trang thanh toán
   ↓
7. Trang thanh toán:
   - Load danh sách hóa đơn chưa thanh toán
   - Hiển thị chi tiết: số tiền, xe, dịch vụ, hạn thanh toán
   - Nút "Thanh toán ngay" → VNPay
```

## Testing

### 1. Test tự động tạo invoice:

```bash
# 1. Tạo appointment
POST /api/appointments
{
  "customerId": "...",
  "vehicleId": "...",
  "serviceCenterId": "...",
  "servicePackageId": "...",
  "appointmentDate": "2025-10-30T10:00:00"
}

# 2. Staff xác nhận
PUT /api/appointments/{id}
{
  "status": "CONFIRMED"
}

# 3. Staff tạo service order
POST /api/service-orders/from-appointment?appointmentId={id}&technicianId={techId}

# 4. Technician hoàn thành
PUT /api/appointments/{id}
{
  "status": "COMPLETED"
}

# 5. Kiểm tra invoice đã được tạo tự động
GET /api/invoices/unpaid
```

### 2. Kết quả mong đợi:

```json
{
  "code": 1000,
  "message": "Danh sách hóa đơn chưa thanh toán.",
  "result": [
    {
      "id": "...",
      "invoiceNumber": "INV-...",
      "status": "PENDING",
      "vehicleLicensePlate": "51A-12345",
      "subtotal": 500000,
      "taxAmount": 50000,
      "discountAmount": 0,
      "totalAmount": 550000,
      "finalAmount": 550000,
      "dueDate": "2025-11-06T..."
    }
  ]
}
```

## Lưu ý quan trọng

1. **Invoice chỉ được tạo khi:**
   - Appointment có ServiceOrder
   - Appointment chuyển sang COMPLETED
   - Chưa có Invoice cho ServiceOrder đó

2. **Tính toán chi phí:**
   - Subtotal = ServicePackage.price
   - Tax = 10% của Subtotal
   - Discount = 0 (mặc định)
   - TotalAmount = Subtotal + Tax - Discount

3. **Hạn thanh toán:**
   - Mặc định: 7 ngày kể từ ngày tạo invoice
   - Có thể cấu hình trong code

4. **Error handling:**
   - Nếu tạo invoice thất bại, không làm fail transaction update appointment
   - Log error để debug

## Troubleshooting

### Invoice không được tạo tự động:

1. **Kiểm tra appointment có ServiceOrder chưa:**
   ```sql
   SELECT * FROM service_orders WHERE appointment_id = '...';
   ```

2. **Kiểm tra appointment status:**
   ```sql
   SELECT id, status, actual_completion 
   FROM service_appointments 
   WHERE id = '...';
   ```

3. **Xem log backend:**
   - Tìm message: "Auto-created invoice for completed appointment..."
   - Hoặc error: "Failed to auto-create invoice..."

### Invoice bị tạo trùng:

- Hệ thống đã có check để tránh tạo trùng
- Kiểm tra method `getInvoiceByServiceOrderId()`

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/invoices` | Tất cả hóa đơn | STAFF/ADMIN |
| GET | `/api/invoices/me` | Hóa đơn của tôi | CUSTOMER |
| GET | `/api/invoices/unpaid` | Hóa đơn chưa thanh toán | CUSTOMER |
| GET | `/api/invoices/{id}` | Chi tiết hóa đơn | ALL |
| POST | `/api/invoices` | Tạo hóa đơn thủ công | STAFF |
| PUT | `/api/invoices/{id}` | Cập nhật hóa đơn | STAFF/ADMIN |

## Next Steps

1. **Chạy migration SQL** để thêm column `status`
2. **Restart backend** để load code mới
3. **Test workflow** từ đầu đến cuối
4. **Kiểm tra UI** trang thanh toán
5. **Thêm email notification** khi invoice được tạo (tùy chọn)

## Contributors

- Backend: Auto-invoice creation logic
- Frontend: Payment UI improvements
- Database: Status column migration
