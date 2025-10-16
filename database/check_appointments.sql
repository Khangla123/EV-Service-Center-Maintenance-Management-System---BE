-- Kiểm tra appointments và vehicle_id

-- 1. Xem tất cả appointments
SELECT 
    id,
    customer_id,
    vehicle_id,
    service_center_id,
    service_package_id,
    appointment_date,
    status,
    created_at
FROM appointments
ORDER BY created_at DESC;

-- 2. Xem appointments với thông tin chi tiết
SELECT 
    a.id as appointment_id,
    a.customer_id,
    c.full_name as customer_name,
    a.vehicle_id,
    v.license_plate,
    vm.manufacturer,
    vm.model,
    a.appointment_date,
    a.status
FROM appointments a
LEFT JOIN customers c ON a.customer_id = c.id
LEFT JOIN vehicles v ON a.vehicle_id = v.id
LEFT JOIN vehicle_models vm ON v.vehicle_model_id = vm.id
ORDER BY a.created_at DESC;

-- 3. Kiểm tra appointments không có vehicle_id
SELECT 
    id,
    customer_id,
    vehicle_id,
    appointment_date,
    status
FROM appointments
WHERE vehicle_id IS NULL;

-- 4. Đếm appointments theo status
SELECT 
    status,
    COUNT(*) as count
FROM appointments
GROUP BY status;

-- 5. Kiểm tra customer có bao nhiêu xe và bao nhiêu appointments
SELECT 
    c.id as customer_id,
    c.full_name,
    u.email,
    COUNT(DISTINCT v.id) as vehicle_count,
    COUNT(DISTINCT a.id) as appointment_count
FROM customers c
LEFT JOIN users u ON c.user_id = u.id
LEFT JOIN vehicles v ON v.customer_id = c.id
LEFT JOIN appointments a ON a.customer_id = c.id
GROUP BY c.id, c.full_name, u.email
ORDER BY appointment_count DESC;
