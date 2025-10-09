-- ============================================
-- EV Service Center Sample Data
-- Insert data in correct order to respect foreign keys
-- ============================================

-- 1. SERVICE CENTERS
INSERT INTO service_centers (id, name, address, phone, email, operating_hours, capacity, is_active) VALUES
                                                                                                        ('550e8400-e29b-41d4-a716-446655440001', 'EV Service Center - District 1', '123 Nguyen Hue, District 1, Ho Chi Minh City', '0281234567', 'district1@evservice.com',
                                                                                                         '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-14:00", "sunday": "closed"}', 15, true),
                                                                                                        ('550e8400-e29b-41d4-a716-446655440002', 'EV Service Center - District 7', '456 Nguyen Thi Thap, District 7, Ho Chi Minh City', '0287654321', 'district7@evservice.com',
                                                                                                         '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-14:00", "sunday": "closed"}', 20, true),
                                                                                                        ('550e8400-e29b-41d4-a716-446655440003', 'EV Service Center - Hanoi', '789 Giang Vo, Ba Dinh, Hanoi', '0241234567', 'hanoi@evservice.com',
                                                                                                         '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-14:00", "sunday": "closed"}', 12, true);

-- 2. USERS
-- Password: all passwords are hashed version of "Password123!" using bcrypt
INSERT INTO users (id, username, email, password_hash, role, full_name, phone, address, is_active, email_verified) VALUES
-- Admins
('650e8400-e29b-41d4-a716-446655440001', 'admin1', 'admin1@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'ADMIN', 'Nguyen Van Admin', '0901234567', '123 Admin Street, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440002', 'admin2', 'admin2@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'ADMIN', 'Tran Thi Manager', '0901234568', '456 Manager Road, Hanoi', true, true),

-- Staff
('650e8400-e29b-41d4-a716-446655440011', 'staff1', 'staff1@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'STAFF', 'Le Van Staff', '0902345671', '11 Staff Ave, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440012', 'staff2', 'staff2@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'STAFF', 'Pham Thi Reception', '0902345672', '22 Staff Blvd, Hanoi', true, true),

-- Technicians
('650e8400-e29b-41d4-a716-446655440021', 'tech1', 'tech1@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'TECHNICIAN', 'Hoang Van Tech', '0903456781', '33 Tech Street, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440022', 'tech2', 'tech2@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'TECHNICIAN', 'Nguyen Van Mechanic', '0903456782', '44 Mechanic Road, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440023', 'tech3', 'tech3@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'TECHNICIAN', 'Tran Van Expert', '0903456783', '55 Expert Lane, District 7', true, true),
('650e8400-e29b-41d4-a716-446655440024', 'tech4', 'tech4@evservice.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'TECHNICIAN', 'Le Thi Specialist', '0903456784', '66 Specialist Ave, Hanoi', true, true),

-- Customers
('650e8400-e29b-41d4-a716-446655440031', 'customer1', 'customer1@gmail.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'CUSTOMER', 'Nguyen Minh Customer', '0904567891', '111 Customer St, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440032', 'customer2', 'customer2@gmail.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'CUSTOMER', 'Tran Thi Buyer', '0904567892', '222 Buyer Rd, Hanoi', true, true),
('650e8400-e29b-41d4-a716-446655440033', 'customer3', 'customer3@gmail.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'CUSTOMER', 'Le Van Owner', '0904567893', '333 Owner Ave, District 7', true, true),
('650e8400-e29b-41d4-a716-446655440034', 'customer4', 'customer4@gmail.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'CUSTOMER', 'Pham Van Driver', '0904567894', '444 Driver Blvd, HCMC', true, true),
('650e8400-e29b-41d4-a716-446655440035', 'customer5', 'customer5@gmail.com', '$2a$10$rKzG7pJKlXGvJlJZJqwKg.yWjhZQvJQYqP5KX0CJhZGZJxF9hNJPi', 'CUSTOMER', 'Hoang Thi Client', '0904567895', '555 Client Lane, Hanoi', true, true);

-- 3. CUSTOMERS
INSERT INTO customers (id, user_id, customer_code, date_of_birth, subscription_expiry, total_spent) VALUES
                                                                                                        ('750e8400-e29b-41d4-a716-446655440031', '650e8400-e29b-41d4-a716-446655440031', 'CUST001', '1985-05-15', '2026-12-31', 15500000),
                                                                                                        ('750e8400-e29b-41d4-a716-446655440032', '650e8400-e29b-41d4-a716-446655440032', 'CUST002', '1990-08-22', '2026-06-30', 8750000),
                                                                                                        ('750e8400-e29b-41d4-a716-446655440033', '650e8400-e29b-41d4-a716-446655440033', 'CUST003', '1988-11-10', '2025-12-31', 22300000),
                                                                                                        ('750e8400-e29b-41d4-a716-446655440034', '650e8400-e29b-41d4-a716-446655440034', 'CUST004', '1992-03-18', '2026-03-31', 5200000),
                                                                                                        ('750e8400-e29b-41d4-a716-446655440035', '650e8400-e29b-41d4-a716-446655440035', 'CUST005', '1987-07-25', '2025-11-30', 12800000);

-- 4. STAFF
INSERT INTO staff (id, user_id, service_center_id, staff_code, specialization, hire_date, salary, is_available) VALUES
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440011', '650e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'STF001', 'Customer Service', '2022-01-15', 12000000, true),
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440012', '650e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440003', 'STF002', 'Reception', '2022-03-20', 11000000, true),
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440021', '650e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440001', 'TECH001', 'Battery Systems', '2021-06-10', 18000000, true),
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440022', '650e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440001', 'TECH002', 'Electric Motors', '2021-08-15', 19000000, true),
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440023', '650e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440002', 'TECH003', 'Power Electronics', '2022-02-01', 20000000, true),
                                                                                                                    ('850e8400-e29b-41d4-a716-446655440024', '650e8400-e29b-41d4-a716-446655440024', '550e8400-e29b-41d4-a716-446655440003', 'TECH004', 'Charging Systems', '2022-05-10', 17500000, true);

-- 5. VEHICLE MODELS
INSERT INTO vehicle_models (id, manufacturer, model, year, battery_capacity, range_km) VALUES
                                                                                           ('950e8400-e29b-41d4-a716-446655440001', 'VinFast', 'VF e34', 2023, 42.00, 300),
                                                                                           ('950e8400-e29b-41d4-a716-446655440002', 'VinFast', 'VF 8', 2024, 87.70, 420),
                                                                                           ('950e8400-e29b-41d4-a716-446655440003', 'VinFast', 'VF 9', 2024, 123.00, 594),
                                                                                           ('950e8400-e29b-41d4-a716-446655440004', 'Tesla', 'Model 3', 2023, 60.00, 491),
                                                                                           ('950e8400-e29b-41d4-a716-446655440005', 'Tesla', 'Model Y', 2024, 75.00, 533),
                                                                                           ('950e8400-e29b-41d4-a716-446655440006', 'Hyundai', 'Ioniq 5', 2023, 72.60, 481),
                                                                                           ('950e8400-e29b-41d4-a716-446655440007', 'Kia', 'EV6', 2024, 77.40, 528);

-- 6. VEHICLES (CORRECTED - VINs are exactly 17 characters)
INSERT INTO vehicles (id, customer_id, vehicle_model_id, vin, license_plate, color, purchase_date, mileage, last_maintenance_date, next_maintenance_date) VALUES
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440031', '950e8400-e29b-41d4-a716-446655440002', 'VF8VN2023A1234567', '51A-12345', 'Pearl White', '2023-06-15', 15200, '2025-09-15', '2025-12-15'),
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440032', '950e8400-e29b-41d4-a716-446655440001', 'VFE34VN2023B78901', '29B-56789', 'Mystic Black', '2023-08-20', 8500, '2025-08-20', '2025-11-20'),
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440033', '950e8400-e29b-41d4-a716-446655440004', '5YJ3E1EA5NF123456', '51F-98765', 'Red Multi-Coat', '2023-03-10', 22000, '2025-09-10', '2025-12-10'),
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440033', '950e8400-e29b-41d4-a716-446655440003', 'VF9VN2024C3456789', '51G-11111', 'Ocean Blue', '2024-01-20', 5600, '2025-10-01', '2026-01-01'),
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440034', '950e8400-e29b-41d4-a716-446655440006', 'KMHH81ZD5NU123456', '30E-22222', 'Gravity Gold', '2023-11-05', 9800, '2025-09-20', '2025-12-20'),
                                                                                                                                                              ('a50e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440035', '950e8400-e29b-41d4-a716-446655440007', 'KNDC341CH00123456', '29C-33333', 'Glacier White', '2024-02-14', 6200, '2025-10-05', '2026-01-05');

-- 7. SERVICE PACKAGES
INSERT INTO service_packages (id, name, description, price, duration_minutes, is_active) VALUES
                                                                                             ('b50e8400-e29b-41d4-a716-446655440001', 'Basic EV Inspection', 'Battery health check, tire rotation, brake inspection, software update', 1500000, 90, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440002', 'Standard Maintenance', 'Complete vehicle inspection, coolant check, air filter replacement, charging system test', 2500000, 120, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440003', 'Premium Service', 'Comprehensive maintenance with battery conditioning, full diagnostics, and detailed report', 4500000, 180, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440004', 'Battery Health Check', 'Deep battery analysis, cell balancing, thermal management system check', 2000000, 60, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440005', 'Software Update Package', 'Latest firmware updates, system calibration, feature activation', 800000, 45, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440006', 'Emergency Repair', 'Urgent diagnostics and repair service', 5000000, 240, true),
                                                                                             ('b50e8400-e29b-41d4-a716-446655440007', 'Annual Full Service', 'Complete yearly maintenance package with all inspections and replacements', 8000000, 300, true);

-- 8. SERVICE APPOINTMENTS
INSERT INTO service_appointments (id, customer_id, vehicle_id, service_center_id, service_package_id, appointment_date, status, notes, estimated_completion, actual_completion) VALUES
-- Completed appointments
('c50e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440031', 'a50e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440002', '2025-09-15 09:00:00+07', 'COMPLETED', 'Regular maintenance check', '2025-09-15 11:00:00+07', '2025-09-15 10:45:00+07'),
('c50e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440032', 'a50e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', 'b50e8400-e29b-41d4-a716-446655440001', '2025-08-20 14:00:00+07', 'COMPLETED', 'First service', '2025-08-20 15:30:00+07', '2025-08-20 15:20:00+07'),
('c50e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440033', 'a50e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440003', '2025-09-10 08:00:00+07', 'COMPLETED', 'Premium service with battery check', '2025-09-10 11:00:00+07', '2025-09-10 11:15:00+07'),

-- In Progress
('c50e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440034', 'a50e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 'b50e8400-e29b-41d4-a716-446655440004', '2025-10-09 09:00:00+07', 'IN_PROGRESS', 'Battery diagnostic requested', '2025-10-09 10:00:00+07', NULL),

-- Confirmed upcoming
('c50e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440035', 'a50e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440003', 'b50e8400-e29b-41d4-a716-446655440002', '2025-10-12 10:00:00+07', 'CONFIRMED', 'Standard maintenance', '2025-10-12 12:00:00+07', NULL),
('c50e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440031', 'a50e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440005', '2025-10-15 14:00:00+07', 'CONFIRMED', 'Software update', '2025-10-15 14:45:00+07', NULL),

-- Pending
('c50e8400-e29b-41d4-a716-446655440007', '750e8400-e29b-41d4-a716-446655440033', 'a50e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440007', '2025-10-20 08:00:00+07', 'PENDING', 'Annual full service', '2025-10-20 13:00:00+07', NULL);

-- 9. SERVICE ORDERS
INSERT INTO service_orders (id, appointment_id, order_code, technician_id, status, start_time, end_time, checklist, diagnosis, work_performed, total_amount) VALUES
                                                                                                                                                                 ('d50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440001', 'SO202509-001', '850e8400-e29b-41d4-a716-446655440021', 'COMPLETED', '2025-09-15 09:00:00+07', '2025-09-15 10:45:00+07',
                                                                                                                                                                  '{"battery_health": "95%", "tire_pressure": "checked", "brake_pads": "70%", "coolant_level": "normal", "software_version": "v2.5.1"}',
                                                                                                                                                                  'Vehicle in good condition. Battery health excellent.',
                                                                                                                                                                  'Performed standard maintenance: tire rotation, brake inspection, coolant top-up, software update to v2.5.1', 2850000),

                                                                                                                                                                 ('d50e8400-e29b-41d4-a716-446655440002', 'c50e8400-e29b-41d4-a716-446655440002', 'SO202508-015', '850e8400-e29b-41d4-a716-446655440024', 'COMPLETED', '2025-08-20 14:00:00+07', '2025-08-20 15:20:00+07',
                                                                                                                                                                  '{"battery_health": "98%", "tire_pressure": "checked", "brake_pads": "90%", "charging_port": "clean"}',
                                                                                                                                                                  'New vehicle first service. All systems nominal.',
                                                                                                                                                                  'Basic inspection completed. Tire pressure adjusted. Charging port cleaned.', 1500000),

                                                                                                                                                                 ('d50e8400-e29b-41d4-a716-446655440003', 'c50e8400-e29b-41d4-a716-446655440003', 'SO202509-008', '850e8400-e29b-41d4-a716-446655440022', 'COMPLETED', '2025-09-10 08:00:00+07', '2025-09-10 11:15:00+07',
                                                                                                                                                                  '{"battery_health": "92%", "battery_cells": "balanced", "thermal_system": "optimal", "power_electronics": "checked", "hvac": "serviced"}',
                                                                                                                                                                  'Minor battery cell imbalance detected and corrected. Thermal management system functioning well.',
                                                                                                                                                                  'Premium service completed: battery conditioning performed, all cells balanced, thermal system checked, HVAC filter replaced, full diagnostic scan completed.', 5200000),

                                                                                                                                                                 ('d50e8400-e29b-41d4-a716-446655440004', 'c50e8400-e29b-41d4-a716-446655440004', 'SO202510-003', '850e8400-e29b-41d4-a716-446655440021', 'IN_PROGRESS', '2025-10-09 09:00:00+07', NULL,
                                                                                                                                                                  '{"battery_health": "checking", "deep_diagnostics": "in_progress"}',
                                                                                                                                                                  'Running comprehensive battery diagnostic test.',
                                                                                                                                                                  'Deep battery analysis in progress. Preliminary results show minor degradation.', 2000000);

-- 10. PARTS
INSERT INTO parts (id, service_center_id, part_code, name, description, category, unit_price, stock_quantity, min_stock_level, supplier) VALUES
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'BRK-PAD-001', 'Brake Pad Set - Front', 'High performance ceramic brake pads for EVs', 'Brakes', 1200000, 25, 10, 'Bosch Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'BRK-PAD-002', 'Brake Pad Set - Rear', 'OEM quality rear brake pads', 'Brakes', 1000000, 30, 10, 'Bosch Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'COOL-001', 'EV Coolant 5L', 'Specialized coolant for battery thermal management', 'Fluids', 450000, 50, 20, 'Mobil Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'FILT-CAB-001', 'Cabin Air Filter', 'HEPA cabin air filter', 'Filters', 350000, 40, 15, 'Mann Filter'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 'TIRE-001', 'EV Tire 235/55R19', 'Low rolling resistance tire for EVs', 'Tires', 3500000, 20, 8, 'Michelin Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440001', 'WIPER-001', 'Wiper Blade Set', 'Premium wiper blades', 'Accessories', 280000, 35, 12, 'Denso Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', 'CHG-CABLE-001', 'Type 2 Charging Cable', '32A charging cable 5m', 'Charging', 4500000, 15, 5, 'Phoenix Contact'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440003', 'BAT-COOL-PUMP', 'Battery Cooling Pump', 'OEM battery cooling pump assembly', 'Battery System', 8500000, 5, 2, 'Continental Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440001', 'HVAC-FILT-001', 'HVAC Filter', 'High efficiency HVAC filter', 'Filters', 420000, 30, 10, 'Valeo Vietnam'),
                                                                                                                                             ('e50e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440002', 'BRK-FLUID-001', 'DOT 4 Brake Fluid 1L', 'High performance brake fluid', 'Fluids', 180000, 45, 15, 'Castrol Vietnam');

-- 11. SERVICE ORDER PARTS
INSERT INTO service_order_parts (id, service_order_id, part_id, quantity, unit_price) VALUES
                                                                                          ('f50e8400-e29b-41d4-a716-446655440001', 'd50e8400-e29b-41d4-a716-446655440001', 'e50e8400-e29b-41d4-a716-446655440003', 2, 450000),
                                                                                          ('f50e8400-e29b-41d4-a716-446655440002', 'd50e8400-e29b-41d4-a716-446655440001', 'e50e8400-e29b-41d4-a716-446655440004', 1, 350000),
                                                                                          ('f50e8400-e29b-41d4-a716-446655440003', 'd50e8400-e29b-41d4-a716-446655440001', 'e50e8400-e29b-41d4-a716-446655440006', 1, 280000),
                                                                                          ('f50e8400-e29b-41d4-a716-446655440004', 'd50e8400-e29b-41d4-a716-446655440003', 'e50e8400-e29b-41d4-a716-446655440009', 1, 420000),
                                                                                          ('f50e8400-e29b-41d4-a716-446655440005', 'd50e8400-e29b-41d4-a716-446655440003', 'e50e8400-e29b-41d4-a716-446655440003', 3, 450000);

-- 12. INVOICES
INSERT INTO invoices (id, service_order_id, customer_id, invoice_number, subtotal, tax_amount, discount_amount, total_amount, issued_at, due_date) VALUES
                                                                                                                                                       ('050e8400-e29b-41d4-a716-446655440001', 'd50e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440031', 'INV-202509-001', 2850000, 285000, 0, 3135000, '2025-09-15 11:00:00+07', '2025-09-22 23:59:59+07'),
                                                                                                                                                       ('050e8400-e29b-41d4-a716-446655440002', 'd50e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440032', 'INV-202508-015', 1500000, 150000, 150000, 1500000, '2025-08-20 15:30:00+07', '2025-08-27 23:59:59+07'),
                                                                                                                                                       ('050e8400-e29b-41d4-a716-446655440003', 'd50e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440033', 'INV-202509-008', 5200000, 520000, 520000, 5200000, '2025-09-10 11:30:00+07', '2025-09-17 23:59:59+07');

-- 13. PAYMENTS
INSERT INTO payments (id, invoice_id, amount, payment_method, status, transaction_id, payment_date, reference_number) VALUES
                                                                                                                          ('150e8400-e29b-41d4-a716-446655440001', '050e8400-e29b-41d4-a716-446655440001', 3135000, 'E_WALLET', 'PAID', 'MOMO-2025091511234567', '2025-09-15 11:15:00+07', 'REF-MOMO-001'),
                                                                                                                          ('150e8400-e29b-41d4-a716-446655440002', '050e8400-e29b-41d4-a716-446655440002', 1500000, 'CARD', 'PAID', 'VISA-2025082015345678', '2025-08-20 15:35:00+07', 'REF-VISA-002'),
                                                                                                                          ('150e8400-e29b-41d4-a716-446655440003', '050e8400-e29b-41d4-a716-446655440003', 5200000, 'BANKING', 'PAID', 'VCB-2025091011456789', '2025-09-10 12:00:00+07', 'REF-VCB-003');

-- 14. MAINTENANCE PLANS
INSERT INTO maintenance_plans (id, vehicle_model_id, service_package_id, interval_km, interval_months, description, is_active) VALUES
                                                                                                                                   ('250e8400-e29b-41d4-a716-446655440001', '950e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440001', 10000, 6, 'VF e34 basic maintenance every 10,000km or 6 months', true),
                                                                                                                                   ('250e8400-e29b-41d4-a716-446655440002', '950e8400-e29b-41d4-a716-446655440002', 'b50e8400-e29b-41d4-a716-446655440002', 15000, 12, 'VF 8 standard maintenance every 15,000km or 12 months', true),
                                                                                                                                   ('250e8400-e29b-41d4-a716-446655440003', '950e8400-e29b-41d4-a716-446655440003', 'b50e8400-e29b-41d4-a716-446655440002', 15000, 12, 'VF 9 standard maintenance every 15,000km or 12 months', true),
                                                                                                                                   ('250e8400-e29b-41d4-a716-446655440004', '950e8400-e29b-41d4-a716-446655440004', 'b50e8400-e29b-41d4-a716-446655440003', 20000, 12, 'Tesla Model 3 premium service every 20,000km or annually', true),
                                                                                                                                   ('250e8400-e29b-41d4-a716-446655440005', '950e8400-e29b-41d4-a716-446655440005', 'b50e8400-e29b-41d4-a716-446655440003', 20000, 12, 'Tesla Model Y premium service every 20,000km or annually', true);

-- 15. CERTIFICATIONS
INSERT INTO certifications (id, staff_id, name, issuing_authority, issue_date, expiry_date, certificate_number, is_active) VALUES
                                                                                                                               ('350e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440021', 'EV Battery Technician Level 3', 'VinFast Technical Academy', '2023-06-15', '2026-06-15', 'VFTA-BAT-L3-2023-001', true),
                                                                                                                               ('350e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440021', 'High Voltage Safety Certification', 'Ministry of Labor Vietnam', '2023-01-10', '2026-01-10', 'MOL-HV-2023-456', true),
                                                                                                                               ('350e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440022', 'Electric Motor Specialist', 'Bosch Technical Training', '2022-11-20', '2025-11-20', 'BOSCH-EMS-2022-789', true),
                                                                                                                               ('350e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440023', 'Power Electronics Certification', 'Continental Training Center', '2023-08-05', '2026-08-05', 'CONT-PE-2023-012', true),
                                                                                                                               ('350e8400-e29b-41d4-a716-446655440005', '850e8400-e29b-41d4-a716-446655440024', 'EV Charging Systems Expert', 'Phoenix Contact Academy', '2023-03-15', '2026-03-15', 'PHX-CHRG-2023-345', true);

-- 16. WORK SCHEDULES
INSERT INTO work_schedules (id, staff_id, date, shift_start, shift_end, is_available) VALUES
-- Week of Oct 9, 2025
('450e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440021', '2025-10-09', '08:00', '17:00', true),
('450e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440022', '2025-10-09', '08:00', '17:00', true),
('450e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440023', '2025-10-09', '13:00', '22:00', true),
('450e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440024', '2025-10-09', '08:00', '17:00', true),

('450e8400-e29b-41d4-a716-446655440011', '850e8400-e29b-41d4-a716-446655440021', '2025-10-10', '08:00', '17:00', true),
('450e8400-e29b-41d4-a716-446655440012', '850e8400-e29b-41d4-a716-446655440022', '2025-10-10', '08:00', '17:00', true),
('450e8400-e29b-41d4-a716-446655440013', '850e8400-e29b-41d4-a716-446655440023', '2025-10-10', '13:00', '22:00', true),

('450e8400-e29b-41d4-a716-446655440021', '850e8400-e29b-41d4-a716-446655440021', '2025-10-11', '08:00', '17:00', true),
('450e8400-e29b-41d4-a716-446655440022', '850e8400-e29b-41d4-a716-446655440022', '2025-10-11', '08:00', '17:00', false),
('450e8400-e29b-41d4-a716-446655440023', '850e8400-e29b-41d4-a716-446655440024', '2025-10-11', '08:00', '17:00', true);

-- 17. NOTIFICATIONS
INSERT INTO notifications (id, customer_id, type, title, message, is_read, scheduled_at, sent_at) VALUES
                                                                                                      ('550e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440031', 'MAINTENANCE_REMINDER', 'Upcoming Maintenance Due', 'Your VF 8 (51A-12345) is due for maintenance on December 15, 2025. Book your appointment today!', true, '2025-09-15 08:00:00+07', '2025-09-15 08:00:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440031', 'SERVICE_COMPLETE', 'Service Completed', 'Your vehicle service has been completed. Total: 3,135,000 VND. Please collect your vehicle.', true, '2025-09-15 10:45:00+07', '2025-09-15 10:45:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440032', 'SERVICE_COMPLETE', 'Service Completed', 'First service completed successfully. Your vehicle is ready for pickup.', true, '2025-08-20 15:20:00+07', '2025-08-20 15:20:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440033', 'MAINTENANCE_REMINDER', 'Maintenance Reminder', 'Your Tesla Model 3 is due for service on December 10, 2025.', false, '2025-10-10 09:00:00+07', '2025-10-10 09:00:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440034', 'APPOINTMENT_UPDATE', 'Appointment Confirmed', 'Your appointment on Oct 9, 2025 at 09:00 AM has been confirmed.', true, '2025-10-08 16:00:00+07', '2025-10-08 16:00:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440035', 'APPOINTMENT_UPDATE', 'Appointment Reminder', 'Reminder: Your appointment is scheduled for Oct 12, 2025 at 10:00 AM.', false, '2025-10-11 10:00:00+07', '2025-10-11 10:00:00+07'),
                                                                                                      ('550e8400-e29b-41d4-a716-446655440007', '750e8400-e29b-41d4-a716-446655440031', 'APPOINTMENT_UPDATE', 'Software Update Appointment', 'Your software update appointment is confirmed for Oct 15, 2025 at 2:00 PM.', false, '2025-10-14 09:00:00+07', NULL);

-- 18. CHAT MESSAGES
INSERT INTO chat_messages (id, service_order_id, sender_id, message, is_staff) VALUES
                                                                                   ('650e8400-e29b-41d4-a716-446655440001', 'd50e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440034', 'Hi, how is my battery diagnostic going?', false),
                                                                                   ('650e8400-e29b-41d4-a716-446655440002', 'd50e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440021', 'Hello! We are currently running the deep battery analysis. Initial results show 88% battery health. We will have complete results in about 30 minutes.', true),
                                                                                   ('650e8400-e29b-41d4-a716-446655440003', 'd50e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440034', 'Thank you! Is that normal for a 2-year-old vehicle?', false),
                                                                                   ('650e8400-e29b-41d4-a716-446655440004', 'd50e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440021', 'Yes, that is within normal range for your vehicle age and usage pattern. We will provide detailed recommendations in the final report.', true),

                                                                                   ('650e8400-e29b-41d4-a716-446655440011', 'd50e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440031', 'Can I pick up my car now?', false),
                                                                                   ('650e8400-e29b-41d4-a716-446655440012', 'd50e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440021', 'Yes! Your VF 8 is ready. Service completed and payment has been processed. You can collect it anytime.', true),
                                                                                   ('650e8400-e29b-41d4-a716-446655440013', 'd50e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440031', 'Great! Will be there in 20 minutes.', false);

-- Update part stock quantities after service orders
UPDATE parts SET stock_quantity = stock_quantity - 2 WHERE id = 'e50e8400-e29b-41d4-a716-446655440003';
UPDATE parts SET stock_quantity = stock_quantity - 1 WHERE id = 'e50e8400-e29b-41d4-a716-446655440004';
UPDATE parts SET stock_quantity = stock_quantity - 1 WHERE id = 'e50e8400-e29b-41d4-a716-446655440006';
UPDATE parts SET stock_quantity = stock_quantity - 1 WHERE id = 'e50e8400-e29b-41d4-a716-446655440009';

-- Update customer total spent
UPDATE customers SET total_spent = (
    SELECT COALESCE(SUM(i.total_amount), 0)
    FROM invoices i
             JOIN payments p ON i.id = p.invoice_id
    WHERE i.customer_id = customers.id AND p.status = 'PAID'
);

-- ============================================
-- Verification Queries (Optional)
-- Run these to check your data
-- ============================================
/*
SELECT 'Service Centers' as table_name, COUNT(*) as count FROM service_centers
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Customers', COUNT(*) FROM customers
UNION ALL SELECT 'Staff', COUNT(*) FROM staff
UNION ALL SELECT 'Vehicle Models', COUNT(*) FROM vehicle_models
UNION ALL SELECT 'Vehicles', COUNT(*) FROM vehicles
UNION ALL SELECT 'Service Packages', COUNT(*) FROM service_packages
UNION ALL SELECT 'Appointments', COUNT(*) FROM service_appointments
UNION ALL SELECT 'Service Orders', COUNT(*) FROM service_orders
UNION ALL SELECT 'Parts', COUNT(*) FROM parts
UNION ALL SELECT 'Service Order Parts', COUNT(*) FROM service_order_parts
UNION ALL SELECT 'Invoices', COUNT(*) FROM invoices
UNION ALL SELECT 'Payments', COUNT(*) FROM payments
UNION ALL SELECT 'Notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'Chat Messages', COUNT(*) FROM chat_messages
UNION ALL SELECT 'Maintenance Plans', COUNT(*) FROM maintenance_plans
UNION ALL SELECT 'Certifications', COUNT(*) FROM certifications
UNION ALL SELECT 'Work Schedules', COUNT(*) FROM work_schedules;
*/