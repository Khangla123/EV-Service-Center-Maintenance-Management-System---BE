# ================================================
# TEST CHECKLIST API - PowerShell Script
# ================================================

$baseUrl = "http://localhost:8080/api/v1"

# ⚠️ THAY ĐỔI USERNAME/PASSWORD NẾU CẦN
$username = "kinn@gmail.com"
$password = "12345"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TEST CHECKLIST API" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

try {
    # Bước 0: Login để lấy token
    Write-Host "0️⃣  Đăng nhập để lấy token..." -ForegroundColor Yellow
    
    $loginBody = @{
        email = $username
        password = $password
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json" `
        -ErrorAction Stop

    $token = $loginResponse.result.token
    
    if (-not $token) {
        Write-Host "❌ Không lấy được token!" -ForegroundColor Red
        Write-Host "   Response: $($loginResponse | ConvertTo-Json)" -ForegroundColor Gray
        exit 1
    }

    Write-Host "✅ Đăng nhập thành công!" -ForegroundColor Green
    Write-Host "   User: $($loginResponse.result.email)" -ForegroundColor White
    Write-Host "   Role: $($loginResponse.result.role)" -ForegroundColor White
    Write-Host "   Token: $($token.Substring(0, [Math]::Min(30, $token.Length)))..." -ForegroundColor Gray

    # Setup authorization header
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }

    # Bước 1: Lấy danh sách appointments
    Write-Host "`n1️⃣  Lấy danh sách appointments..." -ForegroundColor Yellow
    
    $appointmentsResponse = Invoke-RestMethod -Uri "$baseUrl/appointments" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop

    $appointments = $appointmentsResponse.result
    
    if (-not $appointments -or $appointments.Count -eq 0) {
        Write-Host "❌ Không tìm thấy appointment nào!" -ForegroundColor Red
        exit 1
    }

    $testAppointment = $appointments[0]
    Write-Host "✅ Tìm thấy $($appointments.Count) appointments" -ForegroundColor Green
    Write-Host "   Test với appointment đầu tiên:" -ForegroundColor White
    Write-Host "   ID: $($testAppointment.id)" -ForegroundColor White
    Write-Host "   Status: $($testAppointment.status)" -ForegroundColor White
    Write-Host "   Service Package: $($testAppointment.servicePackageName)" -ForegroundColor White
    Write-Host "   Customer: $($testAppointment.customerName)" -ForegroundColor White
    Write-Host "   Date: $($testAppointment.appointmentDate)" -ForegroundColor White

    # Bước 2: Lấy service order theo appointment ID
    Write-Host "`n2️⃣  Lấy service order theo appointment ID..." -ForegroundColor Yellow
    
    try {
        $serviceOrderResponse = Invoke-RestMethod -Uri "$baseUrl/service-orders/appointment/$($testAppointment.id)" `
            -Method Get `
            -Headers $headers `
            -ErrorAction Stop

        $serviceOrder = $serviceOrderResponse.result

        Write-Host "✅ Tìm thấy service order!" -ForegroundColor Green
        Write-Host "   Order Code: $($serviceOrder.orderCode)" -ForegroundColor White
        Write-Host "   Technician ID: $($serviceOrder.technicianId)" -ForegroundColor White
        Write-Host "   Created At: $($serviceOrder.createdAt)" -ForegroundColor White

        # Bước 3: Kiểm tra checklist
        Write-Host "`n3️⃣  Kiểm tra checklist..." -ForegroundColor Yellow
        
        if ($serviceOrder.checklist) {
            Write-Host "`n✅ CHECKLIST CÓ DỮ LIỆU!" -ForegroundColor Green -BackgroundColor DarkGreen
            
            try {
                $checklist = $serviceOrder.checklist | ConvertFrom-Json
                
                Write-Host "`n   📋 Danh sách checklist items:" -ForegroundColor Cyan
                Write-Host "   ═══════════════════════════════════════" -ForegroundColor Cyan
                
                $index = 1
                foreach ($item in $checklist) {
                    Write-Host "`n   [$index] $($item.title)" -ForegroundColor White
                    if ($item.description) {
                        Write-Host "       📝 $($item.description)" -ForegroundColor Gray
                    }
                    if ($item.estimatedMinutes) {
                        Write-Host "       ⏱️  Thời gian ước tính: $($item.estimatedMinutes) phút" -ForegroundColor Gray
                    }
                    if ($item.isRequired) {
                        Write-Host "       ⚠️  Bắt buộc" -ForegroundColor Yellow
                    }
                    $index++
                }
                
                Write-Host "`n   ═══════════════════════════════════════" -ForegroundColor Cyan
                Write-Host "   ✅ CHECKLIST PARSE THÀNH CÔNG!" -ForegroundColor Green
                Write-Host "   📊 Tổng số items: $($checklist.Count)" -ForegroundColor Green
                
            } catch {
                Write-Host "`n   ⚠️  Checklist không parse được JSON!" -ForegroundColor Yellow
                Write-Host "   Error: $_" -ForegroundColor Red
                Write-Host "`n   Raw Checklist JSON:" -ForegroundColor Gray
                Write-Host "   $($serviceOrder.checklist)" -ForegroundColor DarkGray
            }
        } else {
            Write-Host "`n❌ CHECKLIST KHÔNG CÓ DỮ LIỆU (NULL hoặc EMPTY)" -ForegroundColor Red -BackgroundColor DarkRed
            Write-Host "   Checklist value: $($serviceOrder.checklist)" -ForegroundColor Red
            
            Write-Host "`n💡 GỢI Ý:" -ForegroundColor Yellow
            Write-Host "   1. Kiểm tra xem maintenance_plans có checklist_template không" -ForegroundColor Yellow
            Write-Host "   2. Kiểm tra backend code có copy checklist vào service_order không" -ForegroundColor Yellow
            Write-Host "   3. Tạo lại service order mới để test" -ForegroundColor Yellow
        }

        # Hiển thị full service order JSON
        Write-Host "`n📄 Full Service Order JSON:" -ForegroundColor Cyan
        Write-Host "═════════════════════════════════════════════" -ForegroundColor Cyan
        $serviceOrder | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor DarkGray
        Write-Host "═════════════════════════════════════════════" -ForegroundColor Cyan

    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        if ($statusCode -eq 404) {
            Write-Host "❌ Không tìm thấy service order cho appointment này!" -ForegroundColor Red
            Write-Host "   HTTP Status: 404 Not Found" -ForegroundColor Red
            
            Write-Host "`n💡 GỢI Ý: Appointment này chưa có service order." -ForegroundColor Yellow
            Write-Host "   Bạn cần:" -ForegroundColor Yellow
            Write-Host "   1. Bắt đầu công việc từ giao diện technician" -ForegroundColor Yellow
            Write-Host "   2. Hoặc tạo service order qua API:" -ForegroundColor Yellow
            Write-Host "      POST /service-orders/from-appointment/$($testAppointment.id)/assign" -ForegroundColor Gray
            Write-Host "      Body: { `"technicianId`": `"<uuid>`" }" -ForegroundColor Gray
        } else {
            Write-Host "❌ Lỗi khi lấy service order!" -ForegroundColor Red
            Write-Host "   HTTP Status: $statusCode" -ForegroundColor Red
            Write-Host "   Error: $_" -ForegroundColor Red
        }
    }

} catch {
    Write-Host "`n❌ LỖI KHI TEST API!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "`n💡 Lỗi xác thực!" -ForegroundColor Yellow
            Write-Host "   - Kiểm tra username/password có đúng không" -ForegroundColor Yellow
            Write-Host "   - Kiểm tra backend có đang chạy không (port 8080)" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TEST HOÀN TẤT" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
