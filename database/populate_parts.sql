-- ============================================
-- POPULATE PARTS FOR SERVICE CENTERS
-- Script tạo dữ liệu parts cho các trung tâm dịch vụ tại TP.HCM
-- ============================================

-- Xóa dữ liệu cũ (nếu cần)
-- TRUNCATE TABLE service_order_parts CASCADE;
-- DELETE FROM parts;

-- ============================================
-- BƯỚC 1: Lấy các Service Centers từ database
-- ============================================

DO $$
DECLARE
  v_center_q1 UUID;
  v_center_q3 UUID;
  v_center_q7 UUID;
  v_center_tan_binh UUID;
  v_center_thu_duc UUID;
  v_center_binh_thanh UUID;
BEGIN
  -- Lấy ID các trung tâm
  SELECT id INTO v_center_q1 FROM service_centers WHERE name LIKE '%Quận 1%' LIMIT 1;
  SELECT id INTO v_center_q3 FROM service_centers WHERE name LIKE '%Quận 3%' LIMIT 1;
  SELECT id INTO v_center_q7 FROM service_centers WHERE name LIKE '%Quận 7%' LIMIT 1;
  SELECT id INTO v_center_tan_binh FROM service_centers WHERE name LIKE '%Tân Bình%' LIMIT 1;
  SELECT id INTO v_center_thu_duc FROM service_centers WHERE name LIKE '%Thủ Đức%' LIMIT 1;
  SELECT id INTO v_center_binh_thanh FROM service_centers WHERE name LIKE '%Bình Thạnh%' LIMIT 1;

  RAISE NOTICE '==========================================';
  RAISE NOTICE 'DANH SÁCH SERVICE CENTERS';
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'Quận 1: %', v_center_q1;
  RAISE NOTICE 'Quận 3: %', v_center_q3;
  RAISE NOTICE 'Quận 7: %', v_center_q7;
  RAISE NOTICE 'Quận Tân Bình: %', v_center_tan_binh;
  RAISE NOTICE 'Thủ Đức: %', v_center_thu_duc;
  RAISE NOTICE 'Bình Thạnh: %', v_center_binh_thanh;
  RAISE NOTICE '==========================================';
END $$;

-- ============================================
-- BƯỚC 2: Thêm Parts cho TẤT CẢ các Trung tâm
-- Với part_code có prefix theo trung tâm
-- ============================================

INSERT INTO parts (service_center_id, part_code, name, description, category, unit_price, stock_quantity, min_stock_level, supplier, is_active)
SELECT
  sc.id,
  CASE
    WHEN sc.name LIKE '%Quận 1%' THEN 'Q1-' || t.part_code
    WHEN sc.name LIKE '%Quận 3%' THEN 'Q3-' || t.part_code
    WHEN sc.name LIKE '%Quận 7%' THEN 'Q7-' || t.part_code
    WHEN sc.name LIKE '%Tân Bình%' THEN 'TB-' || t.part_code
    WHEN sc.name LIKE '%Thủ Đức%' THEN 'TD-' || t.part_code
    WHEN sc.name LIKE '%Bình Thạnh%' THEN 'BT-' || t.part_code
    ELSE t.part_code
  END as part_code,
  t.name,
  t.description,
  t.category,
  t.unit_price,
  t.stock_quantity,
  t.min_stock_level,
  t.supplier,
  true
FROM service_centers sc
CROSS JOIN (VALUES
  -- PIN & HỆ THỐNG NĂNG LƯỢNG
  ('BATT-60KWH', 'Pin Lithium-ion 60kWh', 'Pin lithium-ion dung lượng 60kWh cho xe điện', 'BATTERY', 25000000, 10, 3, 'BYD Battery Co.'),
  ('BATT-75KWH', 'Pin Lithium-ion 75kWh', 'Pin lithium-ion dung lượng 75kWh cao cấp', 'BATTERY', 32000000, 8, 2, 'CATL Battery Vietnam'),
  ('BATT-90KWH', 'Pin Lithium-ion 90kWh', 'Pin lithium-ion dung lượng 90kWh siêu cao cấp', 'BATTERY', 42000000, 5, 2, 'LG Energy Solution'),
  ('COOLANT-EV', 'Dung dịch làm mát pin EV', 'Dung dịch làm mát chuyên dụng cho pin xe điện', 'COOLING', 200000, 50, 15, 'EV Solutions Vietnam'),
  ('BMS-MODULE', 'Mô-đun quản lý pin BMS', 'Battery Management System module', 'BATTERY', 8500000, 8, 3, 'Tesla Parts Asia'),
  ('CHARGER-22KW', 'Bộ sạc AC 22kW', 'Bộ sạc xoay chiều công suất 22kW', 'ELECTRONICS', 15000000, 6, 2, 'ABB Vietnam'),
  ('CHARGER-50KW', 'Bộ sạc DC 50kW', 'Bộ sạc nhanh DC công suất 50kW', 'ELECTRONICS', 35000000, 3, 1, 'Delta Electronics'),

  -- HỆ THỐNG PHANH
  ('BRAKE-PAD-F', 'Má phanh trước EV', 'Má phanh trước cao cấp cho xe điện', 'BRAKE', 850000, 60, 20, 'Brembo Vietnam'),
  ('BRAKE-PAD-R', 'Má phanh sau EV', 'Má phanh sau cho xe điện', 'BRAKE', 750000, 60, 20, 'Brembo Vietnam'),
  ('BRAKE-DISC-F', 'Đĩa phanh trước', 'Đĩa phanh trước chống rỉ', 'BRAKE', 1200000, 35, 10, 'Bosch Auto Parts'),
  ('BRAKE-DISC-R', 'Đĩa phanh sau', 'Đĩa phanh sau chống rỉ', 'BRAKE', 1000000, 35, 10, 'Bosch Auto Parts'),
  ('BRAKE-FLUID', 'Dầu phanh DOT 4', 'Dầu phanh DOT 4 chuyên dụng xe điện', 'BRAKE', 180000, 80, 25, 'Castrol Vietnam'),
  ('BRAKE-CALIPER-F', 'Cùm phanh trước', 'Cùm phanh trước regenerative', 'BRAKE', 3200000, 15, 5, 'Brembo Vietnam'),
  ('BRAKE-CALIPER-R', 'Cùm phanh sau', 'Cùm phanh sau regenerative', 'BRAKE', 2800000, 15, 5, 'Brembo Vietnam'),

  -- ĐỘNG CƠ ĐIỆN
  ('MOTOR-OIL-EV', 'Dầu động cơ điện', 'Dầu bôi trơn động cơ điện chất lượng cao', 'MOTOR', 350000, 70, 20, 'Mobil EV'),
  ('MOTOR-150KW', 'Động cơ điện 150kW', 'Động cơ điện công suất 150kW', 'MOTOR', 45000000, 5, 2, 'Siemens Electric'),
  ('MOTOR-200KW', 'Động cơ điện 200kW', 'Động cơ điện công suất 200kW cao cấp', 'MOTOR', 58000000, 3, 1, 'Bosch eBike Systems'),
  ('INVERTER-200KW', 'Bộ nghịch lưu 200kW', 'Inverter chuyển đổi DC-AC 200kW', 'ELECTRONICS', 28000000, 5, 2, 'Infineon Technologies'),
  ('INVERTER-300KW', 'Bộ nghịch lưu 300kW', 'Inverter chuyển đổi DC-AC 300kW cao cấp', 'ELECTRONICS', 38000000, 3, 1, 'Infineon Technologies'),

  -- LỐP XE
  ('TIRE-205-55-R16', 'Lốp xe 205/55 R16', 'Lốp xe điện tiết kiệm năng lượng 205/55 R16', 'TIRE', 1600000, 100, 30, 'Michelin Vietnam'),
  ('TIRE-225-45-R18', 'Lốp xe 225/45 R18', 'Lốp xe điện cao cấp 225/45 R18', 'TIRE', 2200000, 80, 25, 'Goodyear Vietnam'),
  ('TIRE-235-50-R19', 'Lốp xe 235/50 R19', 'Lốp xe điện SUV 235/50 R19', 'TIRE', 2800000, 60, 20, 'Pirelli Vietnam'),
  ('TIRE-255-40-R20', 'Lốp xe 255/40 R20', 'Lốp xe điện performance 255/40 R20', 'TIRE', 3500000, 40, 15, 'Pirelli Vietnam'),

  -- HỆ THỐNG TREO
  ('SHOCK-ABSORBER-F', 'Giảm chấn trước', 'Giảm chấn trước thể thao cho EV', 'SUSPENSION', 1500000, 30, 10, 'KYB Vietnam'),
  ('SHOCK-ABSORBER-R', 'Giảm chấn sau', 'Giảm chấn sau cho xe điện', 'SUSPENSION', 1400000, 30, 10, 'KYB Vietnam'),
  ('SPRING-COIL-F', 'Lò xo trước', 'Lò xo treo trước', 'SUSPENSION', 800000, 25, 8, 'Eibach Vietnam'),
  ('SPRING-COIL-R', 'Lò xo sau', 'Lò xo treo sau', 'SUSPENSION', 750000, 25, 8, 'Eibach Vietnam'),
  ('BUSHING-SET', 'Bộ cao su treo', 'Bộ cao su cần treo đầy đủ', 'SUSPENSION', 950000, 35, 12, 'Lemforder Vietnam'),
  ('CONTROL-ARM-F', 'Cần treo trước', 'Cần treo trước cao cấp', 'SUSPENSION', 2200000, 20, 6, 'Lemforder Vietnam'),

  -- ĐIỆN TỬ & CÁP
  ('CABLE-HV-3M', 'Cáp cao áp 3m', 'Cáp truyền điện cao áp 3 mét', 'ELECTRONICS', 900000, 40, 12, 'TE Connectivity'),
  ('CABLE-HV-5M', 'Cáp cao áp 5m', 'Cáp truyền điện cao áp 5 mét', 'ELECTRONICS', 1400000, 30, 10, 'TE Connectivity'),
  ('CABLE-HV-10M', 'Cáp cao áp 10m', 'Cáp truyền điện cao áp 10 mét', 'ELECTRONICS', 2500000, 20, 8, 'TE Connectivity'),
  ('FUSE-400A', 'Cầu chì 400A', 'Cầu chì bảo vệ mạch 400A', 'ELECTRONICS', 180000, 60, 20, 'Littelfuse Asia'),
  ('RELAY-HV', 'Relay cao áp', 'Relay điều khiển mạch cao áp', 'ELECTRONICS', 650000, 50, 15, 'Panasonic Vietnam'),
  ('SENSOR-TEMP', 'Cảm biến nhiệt độ', 'Cảm biến nhiệt độ pin và động cơ', 'ELECTRONICS', 450000, 70, 20, 'Bosch Sensors'),
  ('ECU-CONTROL', 'Bộ điều khiển ECU', 'Electronic Control Unit cho EV', 'ELECTRONICS', 12500000, 8, 3, 'Continental Vietnam'),
  ('SENSOR-PRESSURE', 'Cảm biến áp suất lốp', 'Cảm biến áp suất lốp TPMS', 'ELECTRONICS', 380000, 50, 15, 'Continental Vietnam'),

  -- ĐIỀU HÒA & LỌC
  ('AC-GAS-R134', 'Gas điều hòa R134a', 'Gas điều hòa xe điện (1kg)', 'HVAC', 450000, 50, 15, 'Daikin Vietnam'),
  ('AC-COMPRESSOR', 'Máy nén điều hòa', 'Máy nén điều hòa điện tử', 'HVAC', 8500000, 10, 3, 'Denso Vietnam'),
  ('AC-CONDENSER', 'Giàn nóng điều hòa', 'Giàn nóng điều hòa EV', 'HVAC', 3500000, 12, 4, 'Valeo Vietnam'),
  ('FILTER-CABIN', 'Lọc gió cabin', 'Lọc gió điều hòa cabin than hoạt tính', 'FILTER', 350000, 100, 30, 'Mann Filter'),
  ('FILTER-AIR', 'Lọc gió động cơ', 'Lọc gió động cơ điện', 'FILTER', 250000, 90, 25, 'Mann Filter'),
  ('HEATER-PTC', 'Máy sưởi PTC', 'Máy sưởi PTC cho EV', 'HVAC', 4500000, 10, 3, 'Webasto Vietnam'),

  -- PHỤ KIỆN & KHÁC
  ('WIPER-BLADE', 'Gạt nước kính', 'Gạt nước kính lái cao cấp', 'ACCESSORY', 250000, 120, 35, 'Bosch Auto Parts'),
  ('BULB-LED-H7', 'Bóng đèn LED H7', 'Bóng đèn LED tiết kiệm năng lượng', 'LIGHTING', 320000, 80, 25, 'Philips Vietnam'),
  ('HEADLIGHT-LED', 'Đèn pha LED', 'Đèn pha LED nguyên bộ', 'LIGHTING', 4800000, 15, 5, 'Hella Vietnam'),
  ('INTERIOR-CLEANER', 'Dung dịch vệ sinh nội thất', 'Chuyên dụng cho nội thất da cao cấp', 'CLEANING', 280000, 90, 30, 'Turtle Wax'),
  ('POLISH-WAX', 'Sáp đánh bóng xe', 'Sáp bảo vệ sơn cao cấp', 'CLEANING', 450000, 60, 20, 'Meguiar''s Vietnam'),
  ('WINDOW-WASHER', 'Nước rửa kính', 'Nước rửa kính chống bám bụi (5L)', 'CLEANING', 120000, 150, 45, 'Rain-X Vietnam'),
  ('PAINT-TOUCH-UP', 'Bút chấm sơn', 'Bút chấm sơn đa năng', 'ACCESSORY', 180000, 40, 12, 'Dupli-Color'),
  ('BATTERY-12V', 'Ắc quy phụ 12V', 'Ắc quy phụ 12V cho hệ thống điện tử', 'BATTERY', 1500000, 25, 8, 'Varta Vietnam')
) AS t(part_code, name, description, category, unit_price, stock_quantity, min_stock_level, supplier)
WHERE sc.is_active = true
ON CONFLICT (part_code) DO NOTHING;

-- ============================================
-- BƯỚC 3: Thống kê kết quả
-- ============================================

DO $$
DECLARE
  rec RECORD;
  v_total_centers INTEGER;
  v_total_parts INTEGER;
  v_grand_total_value BIGINT;
BEGIN
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'THỐNG KÊ PARTS THEO TRUNG TÂM';
  RAISE NOTICE '==========================================';

  FOR rec IN
    SELECT
      sc.name,
      sc.address,
      COUNT(p.id) as total_parts,
      SUM(p.unit_price * p.stock_quantity) as total_value
    FROM service_centers sc
    LEFT JOIN parts p ON p.service_center_id = sc.id
    WHERE sc.is_active = true
    GROUP BY sc.id, sc.name, sc.address
    ORDER BY sc.name
  LOOP
    RAISE NOTICE '';
    RAISE NOTICE 'Trung tâm: %', rec.name;
    RAISE NOTICE 'Địa chỉ: %', rec.address;
    RAISE NOTICE 'Tổng số parts: %', rec.total_parts;
    RAISE NOTICE 'Tổng giá trị kho: % VNĐ', COALESCE(rec.total_value, 0);
  END LOOP;

  -- Tổng kết chung
  SELECT COUNT(DISTINCT sc.id), COUNT(p.id), SUM(p.unit_price * p.stock_quantity)
  INTO v_total_centers, v_total_parts, v_grand_total_value
  FROM service_centers sc
  LEFT JOIN parts p ON p.service_center_id = sc.id
  WHERE sc.is_active = true;

  RAISE NOTICE '';
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'TỔNG KẾT';
  RAISE NOTICE '==========================================';
  RAISE NOTICE 'Tổng số trung tâm: %', v_total_centers;
  RAISE NOTICE 'Tổng số parts (tất cả trung tâm): %', v_total_parts;
  RAISE NOTICE 'Tổng giá trị kho (tất cả): % VNĐ', COALESCE(v_grand_total_value, 0);
  RAISE NOTICE '==========================================';
END $$;

-- ============================================
-- BƯỚC 4: Xem chi tiết parts theo trung tâm
-- ============================================

SELECT
  sc.name as service_center,
  p.part_code,
  p.name,
  p.category,
  p.unit_price,
  p.stock_quantity,
  p.supplier,
  (p.unit_price * p.stock_quantity) as total_value
FROM parts p
JOIN service_centers sc ON p.service_center_id = sc.id
WHERE sc.is_active = true
ORDER BY sc.name, p.category, p.name;

-- Thống kê theo category và trung tâm
SELECT
  sc.name as service_center,
  p.category,
  COUNT(*) as total_parts,
  SUM(p.stock_quantity) as total_stock,
  SUM(p.unit_price * p.stock_quantity) as total_value
FROM parts p
JOIN service_centers sc ON p.service_center_id = sc.id
WHERE sc.is_active = true
GROUP BY sc.name, p.category
ORDER BY sc.name, p.category;
