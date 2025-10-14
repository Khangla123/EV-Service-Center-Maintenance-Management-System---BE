-- Kiểm tra dữ liệu trong database

-- 1. Kiểm tra Users
SELECT id, email, full_name, role 
FROM users 
ORDER BY created_at DESC 
LIMIT 10;

-- 2. Kiểm tra Customers và liên kết với Users
SELECT 
    u.id as user_id,
    u.email,
    u.full_name as user_name,
    c.id as customer_id,
    c.full_name as customer_name,
    c.phone
FROM users u
LEFT JOIN customers c ON c.user_id = u.id
WHERE u.role = 'CUSTOMER'
ORDER BY u.created_at DESC;

-- 3. Kiểm tra Vehicle Models
SELECT id, manufacturer, model, year, battery_capacity
FROM vehicle_models
WHERE is_active = true
ORDER BY manufacturer, model;

-- 4. Kiểm tra Vehicles và owners
SELECT 
    v.id as vehicle_id,
    v.vin,
    v.license_plate,
    v.color,
    v.mileage,
    v.purchase_date,
    vm.manufacturer,
    vm.model,
    vm.year,
    vm.battery_capacity,
    c.id as customer_id,
    c.full_name as owner_name,
    u.email as owner_email
FROM vehicles v
JOIN customers c ON v.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicle_models vm ON v.vehicle_model_id = vm.id
WHERE v.is_active = true
ORDER BY v.created_at DESC;

-- 5. Đếm số lượng
SELECT 
    (SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER') as total_customer_users,
    (SELECT COUNT(*) FROM customers) as total_customers,
    (SELECT COUNT(*) FROM vehicle_models WHERE is_active = true) as total_vehicle_models,
    (SELECT COUNT(*) FROM vehicles WHERE is_active = true) as total_vehicles;

-- 6. Kiểm tra user cụ thể (thay YOUR_EMAIL bằng email đang dùng)
-- SELECT 
--     u.id as user_id,
--     u.email,
--     c.id as customer_id,
--     c.full_name,
--     COUNT(v.id) as vehicle_count
-- FROM users u
-- LEFT JOIN customers c ON c.user_id = u.id
-- LEFT JOIN vehicles v ON v.customer_id = c.id
-- WHERE u.email = 'YOUR_EMAIL'
-- GROUP BY u.id, u.email, c.id, c.full_name;
