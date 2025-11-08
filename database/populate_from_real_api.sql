-- ============================================
-- GENERATE DATA FROM REAL API
-- Dựa trên service packages thực tế từ database
-- ============================================

-- Service Packages từ API (12 packages):
-- 1. adb69abd-07db-48d8-80a8-1a47b6227370 - Bảo dưỡng cơ bản
-- 2. 4e49b85e-d760-4498-ba13-dbad9b2030a0 - Bảo dưỡng cơ bản (duplicate)
-- 3. f72b80b0-565d-4163-be8b-861cc76b19e3 - Bảo dưỡng toàn diện
-- 4. 2327a863-d395-4fe8-8a53-bed121bc90ef - Kiểm tra và bảo dưỡng pin
-- 5. de6970ba-4035-43a5-977a-96625bef310d - Thay dầu phanh
-- 6. 3ab14de5-c822-4e55-b8c2-9b26bacdc473 - Bảo dưỡng hệ thống điều hòa
-- 7. d7b65ca2-ea51-4c50-8640-2944baa438bc - Kiểm tra và cân chỉnh hệ thống treo
-- 8. 10262743-8e58-4242-a792-87392f3594ff - Vệ sinh và bảo dưỡng nội thất cao cấp
-- 9. 275bc2cf-0717-47e3-891f-991cd4c56f15 - Cập nhật phần mềm
-- 10. b3377ab1-c1c4-4c24-a1e8-7996a925df50 - Bảo dưỡng định kỳ 10,000 km
-- 11. 8b85fca4-831e-4b6c-9045-764d6f2a4dec - Bảo dưỡng định kỳ 20,000 km
-- 12. 6dae38d8-8705-4857-8c4c-cacf4c579ebd - Gói bảo dưỡng mùa mưa

-- ============================================
-- BƯỚC 1: Thêm cột JSONB (nếu chưa có)
-- ============================================

ALTER TABLE maintenance_plans 
ADD COLUMN IF NOT EXISTS checklist_template JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS suggested_parts JSONB DEFAULT '[]'::jsonb;

ALTER TABLE service_order_parts
ADD COLUMN IF NOT EXISTS is_suggested BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS technician_note TEXT;

-- ============================================
-- BƯỚC 2: Insert/Update Parts (mẫu)
-- ============================================

-- Lấy service center ID đầu tiên hoặc tạo mới
DO $$
DECLARE
  v_service_center_id UUID;
BEGIN
  SELECT id INTO v_service_center_id FROM service_centers LIMIT 1;
  
  IF v_service_center_id IS NULL THEN
    INSERT INTO service_centers (id, name, address, phone, email, capacity)
    VALUES (uuid_generate_v4(), 'Trung tâm bảo dưỡng EV', '123 Láng Hạ, Đống Đa, Hà Nội', '0241234567', 'contact@evservice.vn', 20)
    RETURNING id INTO v_service_center_id;
  END IF;

  -- Insert parts nếu chưa có
  INSERT INTO parts (id, service_center_id, part_code, name, description, category, unit_price, stock_quantity, min_stock_level, is_active)
  SELECT 
    uuid_generate_v4(),
    v_service_center_id,
    part_code,
    name,
    description,
    category,
    unit_price,
    stock_quantity,
    min_stock_level,
    true
  FROM (VALUES
    ('BATT-60KWH', 'Pin Lithium-ion 60kWh', 'Pin lithium-ion dung lượng 60kWh', 'BATTERY', 25000000, 5, 2),
    ('COOLANT-EV', 'Dung dịch làm mát pin EV', 'Dung dịch làm mát cho pin xe điện', 'COOLING', 200000, 20, 5),
    ('BRAKE-PAD-F', 'Má phanh trước EV', 'Má phanh trước cho xe điện', 'BRAKE', 800000, 20, 5),
    ('BRAKE-PAD-R', 'Má phanh sau EV', 'Má phanh sau cho xe điện', 'BRAKE', 700000, 20, 5),
    ('BRAKE-FLUID', 'Dầu phanh DOT 4', 'Dầu phanh DOT 4 chuyên dụng', 'BRAKE', 150000, 30, 10),
    ('MOTOR-OIL-EV', 'Dầu động cơ điện', 'Dầu bôi trơn động cơ điện', 'MOTOR', 300000, 25, 8),
    ('TIRE-205-55-R16', 'Lốp xe 205/55 R16', 'Lốp xe điện size 205/55 R16', 'TIRE', 1500000, 40, 10),
    ('FILTER-CABIN', 'Lọc gió cabin', 'Lọc gió điều hòa cabin', 'OTHER', 300000, 30, 10),
    ('WIPER-BLADE', 'Gạt nước kính', 'Gạt nước kính lái', 'OTHER', 200000, 40, 10),
    ('CABLE-HV-3M', 'Cáp cao áp 3m', 'Cáp truyền điện cao áp', 'ELECTRONICS', 800000, 10, 3),
    ('FUSE-400A', 'Cầu chì 400A', 'Cầu chì bảo vệ 400A', 'ELECTRONICS', 150000, 20, 5),
    ('AC-GAS-R134', 'Gas điều hòa R134a', 'Gas điều hòa xe điện', 'OTHER', 400000, 15, 5),
    ('SHOCK-ABSORBER-F', 'Giảm chấn trước', 'Giảm chấn trước cho EV', 'OTHER', 1200000, 10, 3),
    ('INTERIOR-CLEANER', 'Dung dịch vệ sinh nội thất', 'Chuyên dụng cho nội thất da', 'OTHER', 250000, 30, 10)
  ) AS t(part_code, name, description, category, unit_price, stock_quantity, min_stock_level)
  WHERE NOT EXISTS (SELECT 1 FROM parts WHERE parts.part_code = t.part_code);
  
  RAISE NOTICE 'Parts inserted/updated successfully';
END $$;

-- ============================================
-- BƯỚC 3: Update Maintenance Plans với Templates
-- ============================================

-- 1. Bảo dưỡng cơ bản (adb69abd-07db-48d8-80a8-1a47b6227370)
INSERT INTO maintenance_plans (
  id, service_package_id, interval_km, interval_months, description,
  checklist_template, suggested_parts, is_active
)
SELECT 
  uuid_generate_v4(),
  'adb69abd-07db-48d8-80a8-1a47b6227370'::uuid,
  5000, 3,
  'Bảo dưỡng cơ bản mỗi 5,000 km hoặc 3 tháng',
  '[
    {"title": "Kiểm tra pin", "description": "Kiểm tra dung lượng và sức khỏe pin", "order": 1, "isRequired": true, "estimatedMinutes": 20},
    {"title": "Kiểm tra động cơ", "description": "Kiểm tra hoạt động động cơ điện", "order": 2, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Kiểm tra hệ thống điện", "description": "Kiểm tra cáp và kết nối", "order": 3, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Kiểm tra phanh", "description": "Kiểm tra má phanh và dầu phanh", "order": 4, "isRequired": true, "estimatedMinutes": 10}
  ]'::jsonb,
  (
    SELECT jsonb_agg(
      jsonb_build_object(
        'checklistIndex', idx,
        'checklistTitle', checklist_title,
        'parts', parts_array
      )
    )
    FROM (VALUES
      (0, 'Kiểm tra pin', '[
        {"partCode": "COOLANT-EV", "partName": "Dung dịch làm mát pin", "suggestedQuantity": 1, "usageProbability": 40, "usageNote": "Thay nếu mức thấp", "isCommonlyUsed": true}
      ]'::jsonb),
      (3, 'Kiểm tra phanh', '[
        {"partCode": "BRAKE-FLUID", "partName": "Dầu phanh DOT 4", "suggestedQuantity": 1, "usageProbability": 30, "usageNote": "Thay nếu màu đen", "isCommonlyUsed": true}
      ]'::jsonb)
    ) AS t(idx, checklist_title, parts_array)
  ),
  true
WHERE NOT EXISTS (
  SELECT 1 FROM maintenance_plans 
  WHERE service_package_id = 'adb69abd-07db-48d8-80a8-1a47b6227370'::uuid
) AND EXISTS (
  SELECT 1 FROM service_packages WHERE id = 'adb69abd-07db-48d8-80a8-1a47b6227370'::uuid
);

-- 2. Bảo dưỡng toàn diện (f72b80b0-565d-4163-be8b-861cc76b19e3)
INSERT INTO maintenance_plans (
  id, service_package_id, interval_km, interval_months, description,
  checklist_template, suggested_parts, is_active
)
SELECT 
  uuid_generate_v4(),
  'f72b80b0-565d-4163-be8b-861cc76b19e3'::uuid,
  10000, 6,
  'Bảo dưỡng toàn diện định kỳ',
  '[
    {"title": "Kiểm tra hệ thống pin", "description": "Kiểm tra chi tiết pin, BMS, làm mát", "order": 1, "isRequired": true, "estimatedMinutes": 30},
    {"title": "Kiểm tra động cơ và truyền động", "description": "Kiểm tra động cơ, dầu động cơ", "order": 2, "isRequired": true, "estimatedMinutes": 25},
    {"title": "Kiểm tra hệ thống phanh", "description": "Kiểm tra má phanh, đĩa phanh, dầu phanh", "order": 3, "isRequired": true, "estimatedMinutes": 20},
    {"title": "Kiểm tra hệ thống treo", "description": "Kiểm tra giảm chấn, cân chỉnh độ chụm", "order": 4, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Kiểm tra hệ thống điều hòa", "description": "Kiểm tra điều hòa, lọc gió", "order": 5, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Vệ sinh nội thất", "description": "Vệ sinh ghế, taplo, thảm", "order": 6, "isRequired": true, "estimatedMinutes": 15}
  ]'::jsonb,
  (
    SELECT jsonb_agg(
      jsonb_build_object(
        'checklistIndex', idx,
        'checklistTitle', checklist_title,
        'parts', parts_array
      )
    )
    FROM (VALUES
      (0, 'Kiểm tra hệ thống pin', '[
        {"partCode": "COOLANT-EV", "partName": "Dung dịch làm mát pin", "suggestedQuantity": 2, "usageProbability": 50, "usageNote": "Thay định kỳ", "isCommonlyUsed": true}
      ]'::jsonb),
      (1, 'Kiểm tra động cơ', '[
        {"partCode": "MOTOR-OIL-EV", "partName": "Dầu động cơ điện", "suggestedQuantity": 1, "usageProbability": 40, "usageNote": "Thay nếu cần", "isCommonlyUsed": true}
      ]'::jsonb),
      (2, 'Kiểm tra hệ thống phanh', '[
        {"partCode": "BRAKE-PAD-F", "partName": "Má phanh trước", "suggestedQuantity": 1, "usageProbability": 60, "usageNote": "Thay nếu < 3mm", "isCommonlyUsed": true},
        {"partCode": "BRAKE-PAD-R", "partName": "Má phanh sau", "suggestedQuantity": 1, "usageProbability": 55, "usageNote": "Thay nếu < 3mm", "isCommonlyUsed": true},
        {"partCode": "BRAKE-FLUID", "partName": "Dầu phanh DOT 4", "suggestedQuantity": 1, "usageProbability": 40, "usageNote": "Thay mỗi 2 năm", "isCommonlyUsed": true}
      ]'::jsonb),
      (4, 'Kiểm tra hệ thống điều hòa', '[
        {"partCode": "FILTER-CABIN", "partName": "Lọc gió cabin", "suggestedQuantity": 1, "usageProbability": 70, "usageNote": "Thay định kỳ", "isCommonlyUsed": true},
        {"partCode": "AC-GAS-R134", "partName": "Gas điều hòa", "suggestedQuantity": 1, "usageProbability": 30, "usageNote": "Nạp nếu yếu", "isCommonlyUsed": false}
      ]'::jsonb)
    ) AS t(idx, checklist_title, parts_array)
  ),
  true
WHERE NOT EXISTS (
  SELECT 1 FROM maintenance_plans 
  WHERE service_package_id = 'f72b80b0-565d-4163-be8b-861cc76b19e3'::uuid
) AND EXISTS (
  SELECT 1 FROM service_packages WHERE id = 'f72b80b0-565d-4163-be8b-861cc76b19e3'::uuid
);

-- 3. Kiểm tra và bảo dưỡng pin (2327a863-d395-4fe8-8a53-bed121bc90ef)
INSERT INTO maintenance_plans (
  id, service_package_id, description,
  checklist_template, suggested_parts, is_active
)
SELECT 
  uuid_generate_v4(),
  '2327a863-d395-4fe8-8a53-bed121bc90ef'::uuid,
  'Kiểm tra chuyên sâu hệ thống pin',
  '[
    {"title": "Chẩn đoán pin", "description": "Kiểm tra SOH, SOC, cân bằng cell", "order": 1, "isRequired": true, "estimatedMinutes": 30},
    {"title": "Kiểm tra BMS", "description": "Kiểm tra Battery Management System", "order": 2, "isRequired": true, "estimatedMinutes": 20},
    {"title": "Kiểm tra hệ thống làm mát", "description": "Kiểm tra làm mát pin", "order": 3, "isRequired": true, "estimatedMinutes": 20},
    {"title": "Cập nhật firmware BMS", "description": "Update phần mềm quản lý pin", "order": 4, "isRequired": false, "estimatedMinutes": 20}
  ]'::jsonb,
  '[
    {
      "checklistIndex": 0,
      "checklistTitle": "Chẩn đoán pin",
      "parts": [
        {"partCode": "BATT-60KWH", "partName": "Pin Lithium 60kWh", "suggestedQuantity": 1, "usageProbability": 15, "usageNote": "Thay nếu hỏng nặng", "isCommonlyUsed": false}
      ]
    },
    {
      "checklistIndex": 2,
      "checklistTitle": "Kiểm tra hệ thống làm mát",
      "parts": [
        {"partCode": "COOLANT-EV", "partName": "Dung dịch làm mát", "suggestedQuantity": 3, "usageProbability": 70, "usageNote": "Thay hoàn toàn", "isCommonlyUsed": true}
      ]
    }
  ]'::jsonb,
  true
WHERE NOT EXISTS (
  SELECT 1 FROM maintenance_plans 
  WHERE service_package_id = '2327a863-d395-4fe8-8a53-bed121bc90ef'::uuid
) AND EXISTS (
  SELECT 1 FROM service_packages WHERE id = '2327a863-d395-4fe8-8a53-bed121bc90ef'::uuid
);

-- 4. Thay dầu phanh (de6970ba-4035-43a5-977a-96625bef310d)
INSERT INTO maintenance_plans (
  id, service_package_id, description,
  checklist_template, suggested_parts, is_active
)
SELECT 
  uuid_generate_v4(),
  'de6970ba-4035-43a5-977a-96625bef310d'::uuid,
  'Bảo dưỡng hệ thống phanh',
  '[
    {"title": "Kiểm tra má phanh", "description": "Đo độ dày má phanh", "order": 1, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Kiểm tra đĩa phanh", "description": "Kiểm tra độ mòn đĩa", "order": 2, "isRequired": true, "estimatedMinutes": 10},
    {"title": "Thay dầu phanh", "description": "Thay hoàn toàn dầu phanh", "order": 3, "isRequired": true, "estimatedMinutes": 30},
    {"title": "Test phanh", "description": "Chạy thử kiểm tra phanh", "order": 4, "isRequired": true, "estimatedMinutes": 20}
  ]'::jsonb,
  '[
    {
      "checklistIndex": 0,
      "checklistTitle": "Kiểm tra má phanh",
      "parts": [
        {"partCode": "BRAKE-PAD-F", "partName": "Má phanh trước", "suggestedQuantity": 1, "usageProbability": 80, "usageNote": "Thay nếu < 3mm", "isCommonlyUsed": true},
        {"partCode": "BRAKE-PAD-R", "partName": "Má phanh sau", "suggestedQuantity": 1, "usageProbability": 75, "usageNote": "Thay nếu < 3mm", "isCommonlyUsed": true}
      ]
    },
    {
      "checklistIndex": 2,
      "checklistTitle": "Thay dầu phanh",
      "parts": [
        {"partCode": "BRAKE-FLUID", "partName": "Dầu phanh DOT 4", "suggestedQuantity": 1, "usageProbability": 100, "usageNote": "Thay mới", "isCommonlyUsed": true}
      ]
    }
  ]'::jsonb,
  true
WHERE NOT EXISTS (
  SELECT 1 FROM maintenance_plans 
  WHERE service_package_id = 'de6970ba-4035-43a5-977a-96625bef310d'::uuid
) AND EXISTS (
  SELECT 1 FROM service_packages WHERE id = 'de6970ba-4035-43a5-977a-96625bef310d'::uuid
);

-- 5. Bảo dưỡng hệ thống điều hòa (3ab14de5-c822-4e55-b8c2-9b26bacdc473)
INSERT INTO maintenance_plans (
  id, service_package_id, description,
  checklist_template, suggested_parts, is_active
)
SELECT 
  uuid_generate_v4(),
  '3ab14de5-c822-4e55-b8c2-9b26bacdc473'::uuid,
  'Vệ sinh và bảo dưỡng điều hòa',
  '[
    {"title": "Vệ sinh dàn lạnh", "description": "Vệ sinh dàn lạnh điều hòa", "order": 1, "isRequired": true, "estimatedMinutes": 20},
    {"title": "Thay lọc gió cabin", "description": "Thay lọc gió mới", "order": 2, "isRequired": true, "estimatedMinutes": 10},
    {"title": "Kiểm tra gas điều hòa", "description": "Kiểm tra áp suất gas", "order": 3, "isRequired": true, "estimatedMinutes": 15},
    {"title": "Nạp gas (nếu cần)", "description": "Nạp thêm gas nếu yếu", "order": 4, "isRequired": false, "estimatedMinutes": 15}
  ]'::jsonb,
  '[
    {
      "checklistIndex": 1,
      "checklistTitle": "Thay lọc gió cabin",
      "parts": [
        {"partCode": "FILTER-CABIN", "partName": "Lọc gió cabin", "suggestedQuantity": 1, "usageProbability": 100, "usageNote": "Thay mới", "isCommonlyUsed": true}
      ]
    },
    {
      "checklistIndex": 3,
      "checklistTitle": "Nạp gas",
      "parts": [
        {"partCode": "AC-GAS-R134", "partName": "Gas điều hòa", "suggestedQuantity": 1, "usageProbability": 50, "usageNote": "Nạp nếu yếu", "isCommonlyUsed": true}
      ]
    }
  ]'::jsonb,
  true
WHERE NOT EXISTS (
  SELECT 1 FROM maintenance_plans 
  WHERE service_package_id = '3ab14de5-c822-4e55-b8c2-9b26bacdc473'::uuid
) AND EXISTS (
  SELECT 1 FROM service_packages WHERE id = '3ab14de5-c822-4e55-b8c2-9b26bacdc473'::uuid
);

-- ============================================
-- VERIFICATION
-- ============================================

SELECT 
  mp.id as plan_id,
  sp.name as service_package,
  jsonb_array_length(mp.checklist_template) as checklist_items,
  jsonb_array_length(mp.suggested_parts) as parts_groups,
  mp.is_active
FROM maintenance_plans mp
INNER JOIN service_packages sp ON mp.service_package_id = sp.id
ORDER BY sp.name;

-- View parts inventory
SELECT 
  category,
  COUNT(*) as part_count,
  SUM(stock_quantity) as total_stock
FROM parts
WHERE is_active = true
GROUP BY category
ORDER BY category;
