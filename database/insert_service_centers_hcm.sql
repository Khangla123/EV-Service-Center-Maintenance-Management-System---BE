-- ===================================================================
-- SERVICE CENTERS DATA - TP. HỒ CHÍ MINH
-- Generated: November 21, 2025
-- Description: Dữ liệu các trung tâm dịch vụ EV tại TP.HCM
-- ===================================================================

INSERT INTO service_centers (id, name, address, phone, email, operating_hours, capacity, is_active) VALUES
('f87275c7-21c4-4b3e-a1f2-8d9e3f1a2b5c', 
 'EV Service Center Quận 1', 
 '123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM', 
 '028-1234567', 
 'q1@evservice.vn',
 '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-18:00", "sunday": "Closed"}'::jsonb,
 15,
 true),

('2bdd54d8-6e0a-4f9c-b8d3-7c2e9a1f4b3d',
 'EV Service Center Quận 3',
 '456 Lê Văn Sỹ, Phường 14, Quận 3, TP.HCM',
 '028-4567890',
 'q3@evservice.vn',
 '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-17:30", "sunday": "Closed"}'::jsonb,
 50,
 true),

('0ab3ecaa-b3e9-4d2c-a5f6-1c8b7e4d2a9f',
 'EV Service Center Quận 7',
 '789 Nguyễn Văn Linh, Phường Tân Phú, Quận 7, TP.HCM',
 '028-9876543',
 'q7@evservice.vn',
 '{"monday": "08:00-19:00", "tuesday": "08:00-19:00", "wednesday": "08:00-19:00", "thursday": "08:00-19:00", "friday": "08:00-19:00", "saturday": "08:00-18:00", "sunday": "Closed"}'::jsonb,
 80,
 true),

('ab604dfa-f321-4b5e-b6d8-9c3e2a7f5d1b',
 'EV Service Center Quận Tân Bình',
 '234 Hoàng Văn Thụ, Phường 4, Quận Tân Bình, TP.HCM',
 '028-3654789',
 'tanbinhev@evservice.vn',
 '{"monday": "07:30-18:00", "tuesday": "07:30-18:00", "wednesday": "07:30-18:00", "thursday": "07:30-18:00", "friday": "07:30-18:00", "saturday": "08:00-17:00", "sunday": "Closed"}'::jsonb,
 40,
 true),

('a895f176-a8b5-4c3d-9e2f-7d4a6b1c8e3f',
 'EV Service Center Thủ Đức',
 '567 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP.HCM',
 '028-7890123',
 'thuduc@evservice.vn',
 '{"monday": "08:00-18:00", "tuesday": "08:00-18:00", "wednesday": "08:00-18:00", "thursday": "08:00-18:00", "friday": "08:00-18:00", "saturday": "08:00-16:00", "sunday": "Closed"}'::jsonb,
 28,
 true),

('824fbef7-3891-4d5c-a2b6-8e7d3c9f1a4b',
 'EV Service Center Bình Thạnh',
 '890 Xô Viết Nghệ Tĩnh, Phường 25, Quận Bình Thạnh, TP.HCM',
 '028-3890124',
 'binhthanh@evservice.vn',
 '{"monday": "07:30-17:30", "tuesday": "07:30-17:30", "wednesday": "07:30-17:30", "thursday": "07:30-17:30", "friday": "07:30-17:30", "saturday": "08:00-16:00", "sunday": "Closed"}'::jsonb,
 22,
 true);

-- Verify insertion
SELECT 
    name,
    address,
    phone,
    email,
    capacity,
    is_active
FROM service_centers
ORDER BY name;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Đã thêm 6 trung tâm dịch vụ EV tại TP.HCM';
    RAISE NOTICE '📍 Quận 1 - 15 chỗ';
    RAISE NOTICE '📍 Quận 3 - 50 chỗ';
    RAISE NOTICE '📍 Quận 7 - 80 chỗ';
    RAISE NOTICE '📍 Tân Bình - 40 chỗ';
    RAISE NOTICE '📍 Thủ Đức - 28 chỗ';
    RAISE NOTICE '📍 Bình Thạnh - 22 chỗ';
END $$;
