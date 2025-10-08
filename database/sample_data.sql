-- Sample Data Script for EV Service Center Management System
-- PostgreSQL Sample Data - Service Centers, Vehicle Models & Vehicles Only

-- Insert sample service centers
INSERT INTO service_centers (id, name, address, phone, email, operating_hours, capacity, is_active, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'EV Service Center Quận 1', '123 Đường Lê Lợi, Quận 1, TP.HCM', '028-3822-1234', 'q1@evservice.com', '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-17:00", "sunday": "09:00-16:00"}', 15, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'EV Service Center Quận 3', '456 Đường Võ Văn Tần, Quận 3, TP.HCM', '028-3933-5678', 'q3@evservice.com', '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-17:00", "sunday": "CLOSED"}', 12, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'EV Service Center Quận 7', '789 Đường Nguyễn Thị Thập, Quận 7, TP.HCM', '028-3774-9012', 'q7@evservice.com', '{"monday": "07:30-19:00", "tuesday": "07:30-19:00", "wednesday": "07:30-19:00", "thursday": "07:30-19:00", "friday": "07:30-19:00", "saturday": "08:00-18:00", "sunday": "09:00-17:00"}', 20, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440004', 'EV Service Center Thủ Đức', '321 Đường Võ Văn Ngân, TP.Thủ Đức, TP.HCM', '028-3896-3456', 'thuduc@evservice.com', '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-16:00", "sunday": "CLOSED"}', 18, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440005', 'EV Service Center Hà Nội', '567 Đường Láng Hạ, Đống Đa, Hà Nội', '024-3573-7890', 'hanoi@evservice.com', '{"monday": "08:00-17:30", "tuesday": "08:00-17:30", "wednesday": "08:00-17:30", "thursday": "08:00-17:30", "friday": "08:00-17:30", "saturday": "08:00-16:30", "sunday": "09:00-15:00"}', 25, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440006', 'EV Service Center Đà Nẵng', '888 Đường Nguyễn Văn Linh, Hải Châu, Đà Nẵng', '0236-3888-999', 'danang@evservice.com', '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-17:00", "sunday": "09:00-16:00"}', 14, true, NOW(), NOW());

-- Insert sample vehicle models
INSERT INTO vehicle_models (id, manufacturer, model, year, battery_capacity, range_km, created_at) VALUES
-- VinFast Models
('850e8400-e29b-41d4-a716-446655440001', 'VinFast', 'VF 6', 2024, 59.6, 285, NOW()),
('850e8400-e29b-41d4-a716-446655440002', 'VinFast', 'VF 8', 2023, 87.7, 420, NOW()),
('850e8400-e29b-41d4-a716-446655440003', 'VinFast', 'VF 9', 2023, 123.0, 438, NOW()),

-- Tesla Models
('850e8400-e29b-41d4-a716-446655440004', 'Tesla', 'Model 3', 2022, 75.0, 448, NOW()),
('850e8400-e29b-41d4-a716-446655440005', 'Tesla', 'Model Y', 2023, 75.0, 525, NOW()),
('850e8400-e29b-41d4-a716-446655440006', 'Tesla', 'Model S', 2023, 100.0, 652, NOW()),

-- Hyundai Models
('850e8400-e29b-41d4-a716-446655440007', 'Hyundai', 'IONIQ 5', 2022, 77.4, 384, NOW()),
('850e8400-e29b-41d4-a716-446655440008', 'Hyundai', 'IONIQ 6', 2023, 77.4, 449, NOW()),

-- Kia Models
('850e8400-e29b-41d4-a716-446655440009', 'Kia', 'EV6', 2023, 77.4, 425, NOW()),
('850e8400-e29b-41d4-a716-446655440010', 'Kia', 'Niro EV', 2022, 64.8, 385, NOW()),

-- BMW Models
('850e8400-e29b-41d4-a716-446655440011', 'BMW', 'iX3', 2022, 80.0, 460, NOW()),
('850e8400-e29b-41d4-a716-446655440012', 'BMW', 'i4', 2023, 83.9, 590, NOW()),

-- Audi Models
('850e8400-e29b-41d4-a716-446655440013', 'Audi', 'e-tron GT', 2023, 93.4, 488, NOW()),
('850e8400-e29b-41d4-a716-446655440014', 'Audi', 'Q4 e-tron', 2022, 82.0, 520, NOW()),

-- Mercedes Models
('850e8400-e29b-41d4-a716-446655440015', 'Mercedes-Benz', 'EQS', 2023, 108.0, 770, NOW()),
('850e8400-e29b-41d4-a716-446655440016', 'Mercedes-Benz', 'EQC', 2022, 80.0, 471, NOW());

-- Insert sample vehicles (with reference to vehicle_model_id)
INSERT INTO vehicles (id, vehicle_model_id, vin, license_plate, color, purchase_date, mileage, last_maintenance_date, next_maintenance_date, is_active, created_at, updated_at) VALUES
-- VinFast Vehicles
('750e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440002', 'WVWZZZ1JZYW123456', '51A-12345', 'Trắng Ngọc Trai', '2023-03-15', 15000, '2024-09-15', '2024-12-15', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440003', 'WVWZZZ1JZYW654321', '51A-67890', 'Đen Obsidian', '2023-06-20', 8500, '2024-08-20', '2024-11-20', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440001', 'WVWZZZ1JZYW789012', '51B-11111', 'Xanh Dương', '2024-01-10', 5200, '2024-07-10', '2024-10-10', true, NOW(), NOW()),

-- Tesla Vehicles
('750e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440004', 'TESLA123456789012', '51C-22222', 'Xanh Midnight', '2022-08-10', 25000, '2024-09-25', '2024-12-25', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440005', '850e8400-e29b-41d4-a716-446655440005', 'TESLA987654321098', '51C-33333', 'Đỏ Cherry', '2023-01-25', 12000, '2024-08-15', '2024-11-15', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440006', '850e8400-e29b-41d4-a716-446655440006', 'TESLA555666777888', '51D-44444', 'Trắng Pearl', '2023-05-30', 9800, '2024-09-10', '2024-12-10', true, NOW(), NOW()),

-- Hyundai Vehicles
('750e8400-e29b-41d4-a716-446655440007', '850e8400-e29b-41d4-a716-446655440007', 'HYUNDAI12345ABCDE', '51E-55555', 'Bạc Metallic', '2022-11-30', 18000, '2024-08-30', '2024-11-30', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440008', '850e8400-e29b-41d4-a716-446655440008', 'HYUNDAI67890FGHIJ', '51E-66666', 'Tím Cosmic', '2023-07-18', 11500, '2024-09-05', '2024-12-05', true, NOW(), NOW()),

-- Kia Vehicles
('750e8400-e29b-41d4-a716-446655440009', '850e8400-e29b-41d4-a716-446655440009', 'KIA9876543210FGHI', '51F-77777', 'Xám Gravity', '2023-04-18', 9500, '2024-08-18', '2024-11-18', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440010', '850e8400-e29b-41d4-a716-446655440010', 'KIA1111222233334', '51F-88888', 'Xanh Lá Moss', '2022-12-05', 22000, '2024-09-20', '2024-12-20', true, NOW(), NOW()),

-- BMW Vehicles
('750e8400-e29b-41d4-a716-446655440011', '850e8400-e29b-41d4-a716-446655440011', 'BMW123ABC456DEF78', '30A-99999', 'Trắng Alpine', '2022-09-12', 22000, '2024-07-12', '2024-10-12', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440012', '850e8400-e29b-41d4-a716-446655440012', 'BMW987XYZ654MNO12', '30A-10101', 'Xanh Dương Storm', '2023-03-28', 14500, '2024-08-25', '2024-11-25', true, NOW(), NOW()),

-- Audi Vehicles
('750e8400-e29b-41d4-a716-446655440013', '850e8400-e29b-41d4-a716-446655440013', 'AUDI789XYZ012GHI3', '30B-20202', 'Xanh Dương Kemora', '2023-07-05', 6800, '2024-09-01', '2024-12-01', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440014', '850e8400-e29b-41d4-a716-446655440014', 'AUDI456QRS789TUV0', '30B-30303', 'Đen Mythos', '2022-10-20', 19500, '2024-08-10', '2024-11-10', true, NOW(), NOW()),

-- Mercedes Vehicles
('750e8400-e29b-41d4-a716-446655440015', '850e8400-e29b-41d4-a716-446655440015', 'MERC123456789ABC', '30C-40404', 'Bạc Selenite', '2023-02-14', 12800, '2024-09-14', '2024-12-14', true, NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440016', '850e8400-e29b-41d4-a716-446655440016', 'MERC987654321XYZ', '30C-50505', 'Đỏ Designo Hyacinth', '2022-06-30', 26500, '2024-08-05', '2024-11-05', true, NOW(), NOW());

-- Display summary
SELECT 'Service Centers inserted: ' || COUNT(*) as summary FROM service_centers;
SELECT 'Vehicle Models inserted: ' || COUNT(*) as summary FROM vehicle_models;
SELECT 'Vehicles inserted: ' || COUNT(*) as summary FROM vehicles;
