-- ===================================================================
-- SERVICE PACKAGES DATA
-- EV Service Center Maintenance Management System
-- Description: Standard service packages for electric vehicles
-- ===================================================================

-- Insert Service Packages
INSERT INTO service_packages (id, name, description, price, duration_minutes, is_active, created_at) VALUES
-- Basic Maintenance Packages
(
    uuid_generate_v4(),
    'Bảo dưỡng cơ bản',
    'Kiểm tra tổng thể xe điện, kiểm tra hệ thống điện, kiểm tra phanh, đèn, lốp xe. Bao gồm rửa xe miễn phí.',
    500000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng định kỳ 10,000 km',
    'Bảo dưỡng định kỳ cho xe đã chạy 10,000km. Kiểm tra và thay dầu phanh, kiểm tra hệ thống treo, kiểm tra pin, cân chỉnh bánh xe.',
    1200000,
    120,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng định kỳ 20,000 km',
    'Bảo dưỡng toàn diện cho xe đã chạy 20,000km. Bao gồm tất cả dịch vụ của gói 10,000km cộng thêm kiểm tra động cơ điện, thay lọc gió điều hòa.',
    2000000,
    180,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng định kỳ 40,000 km',
    'Bảo dưỡng chuyên sâu cho xe đã chạy 40,000km. Kiểm tra chi tiết hệ thống pin, động cơ điện, hệ thống làm mát, thay má phanh nếu cần.',
    3500000,
    240,
    true,
    CURRENT_TIMESTAMP
),

-- Battery Services
(
    uuid_generate_v4(),
    'Kiểm tra sức khỏe pin',
    'Chẩn đoán chi tiết tình trạng pin: dung lượng, chu kỳ sạc, nhiệt độ hoạt động, hiệu suất sạc. Báo cáo chi tiết tình trạng pin.',
    800000,
    90,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng hệ thống pin',
    'Vệ sinh và bảo dưỡng hệ thống pin, kiểm tra và thay thế các cell pin yếu, cân bằng điện áp các cell, kiểm tra hệ thống quản lý pin (BMS).',
    2500000,
    180,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Nâng cấp phần mềm BMS',
    'Cập nhật phần mềm quản lý pin (Battery Management System) lên phiên bản mới nhất, tối ưu hóa hiệu suất sạc và tuổi thọ pin.',
    1500000,
    60,
    true,
    CURRENT_TIMESTAMP
),

-- Motor & Drivetrain Services
(
    uuid_generate_v4(),
    'Kiểm tra động cơ điện',
    'Chẩn đoán và kiểm tra chi tiết động cơ điện: hiệu suất, nhiệt độ hoạt động, độ rung, tiếng ồn. Kiểm tra hệ thống truyền động.',
    1000000,
    90,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng hệ thống truyền động',
    'Kiểm tra và bảo dưỡng hộp số (nếu có), kiểm tra và thay dầu hộp số, kiểm tra các khớp truyền động, bạc đạn.',
    1800000,
    120,
    true,
    CURRENT_TIMESTAMP
),

-- Brake System Services
(
    uuid_generate_v4(),
    'Kiểm tra hệ thống phanh',
    'Kiểm tra độ dày má phanh, đĩa phanh, dầu phanh, hệ thống phanh tái sinh (regenerative braking). Điều chỉnh nếu cần.',
    600000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Thay má phanh trước/sau',
    'Thay má phanh mới cho bánh trước hoặc bánh sau, bao gồm kiểm tra đĩa phanh và điều chỉnh hệ thống phanh.',
    1500000,
    90,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Thay toàn bộ hệ thống phanh',
    'Thay má phanh và đĩa phanh cả 4 bánh, thay dầu phanh mới, kiểm tra và hiệu chỉnh hệ thống phanh ABS.',
    4000000,
    180,
    true,
    CURRENT_TIMESTAMP
),

-- Tire Services
(
    uuid_generate_v4(),
    'Cân chỉnh và cân bằng bánh xe',
    'Cân chỉnh độ chụm bánh xe (wheel alignment), cân bằng động bánh xe (wheel balancing) cho cả 4 bánh.',
    800000,
    90,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Xoay vị trí lốp xe',
    'Xoay vị trí 4 bánh xe để đảm bảo độ mòn đều, kéo dài tuổi thọ lốp. Bao gồm kiểm tra áp suất lốp.',
    400000,
    45,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Thay 1 lốp xe mới',
    'Thay 1 lốp xe mới chính hãng (chưa bao gồm giá lốp), cân bằng bánh xe, kiểm tra áp suất.',
    300000,
    30,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Thay bộ 4 lốp xe mới',
    'Thay bộ 4 lốp xe mới chính hãng (chưa bao gồm giá lốp), cân chỉnh và cân bằng toàn bộ, kiểm tra hệ thống treo.',
    1000000,
    120,
    true,
    CURRENT_TIMESTAMP
),

-- Air Conditioning Services
(
    uuid_generate_v4(),
    'Vệ sinh hệ thống điều hòa',
    'Vệ sinh dàn lạnh, dàn nóng, thay lọc gió điều hòa, khử mùi khoang cabin.',
    700000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Bảo dưỡng hệ thống điều hòa',
    'Kiểm tra và nạp gas điều hòa, kiểm tra compressor, thay lọc gió, vệ sinh toàn bộ hệ thống.',
    1500000,
    90,
    true,
    CURRENT_TIMESTAMP
),

-- Charging System Services
(
    uuid_generate_v4(),
    'Kiểm tra hệ thống sạc',
    'Kiểm tra cổng sạc, cáp sạc, bộ sạc xe (OBC - On Board Charger), kiểm tra tốc độ và hiệu suất sạc.',
    800000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Sửa chữa hệ thống sạc',
    'Sửa chữa và thay thế linh kiện hệ thống sạc: cổng sạc, bộ chuyển đổi, dây cáp sạc.',
    2000000,
    120,
    true,
    CURRENT_TIMESTAMP
),

-- Electrical System Services
(
    uuid_generate_v4(),
    'Kiểm tra hệ thống điện',
    'Chẩn đoán toàn bộ hệ thống điện: mạch điện, cầu chì, rơ le, đèn chiếu sáng, hệ thống giải trí.',
    600000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Cập nhật phần mềm hệ thống',
    'Cập nhật phần mềm ECU, hệ thống giải trí, hệ thống an toàn lên phiên bản mới nhất từ nhà sản xuất.',
    1200000,
    90,
    true,
    CURRENT_TIMESTAMP
),

-- Safety & ADAS Services
(
    uuid_generate_v4(),
    'Kiểm tra hệ thống an toàn',
    'Kiểm tra túi khí (airbag), hệ thống chống bó cứng phanh (ABS), hệ thống cân bằng điện tử (ESC), cảm biến va chạm.',
    800000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Hiệu chỉnh hệ thống ADAS',
    'Hiệu chỉnh các hệ thống hỗ trợ lái xe nâng cao: cảnh báo làn đường, phanh tự động khẩn cấp, cảm biến đỗ xe.',
    1500000,
    90,
    true,
    CURRENT_TIMESTAMP
),

-- Detailing & Cosmetic Services
(
    uuid_generate_v4(),
    'Rửa xe và vệ sinh nội thất cơ bản',
    'Rửa xe bên ngoài, hút bụi nội thất, lau chùi và vệ sinh khoang cabin.',
    200000,
    30,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Vệ sinh nội thất cao cấp',
    'Vệ sinh sâu nội thất: giặt ghế nỉ/da, vệ sinh trần xe, cửa xe, taplo, khử mùi và khử khuẩn chuyên nghiệp.',
    1000000,
    120,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Đánh bóng và phủ ceramic',
    'Đánh bóng toàn bộ thân xe, phủ lớp ceramic bảo vệ sơn xe, chống trầy xước và oxy hóa.',
    3000000,
    240,
    true,
    CURRENT_TIMESTAMP
),

-- Emergency & Rescue Services
(
    uuid_generate_v4(),
    'Cứu hộ và kéo xe (trong thành phố)',
    'Dịch vụ cứu hộ và kéo xe trong phạm vi thành phố HCM khi xe gặp sự cố, hết pin, không khởi động được.',
    1500000,
    60,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Sạc pin khẩn cấp tại chỗ',
    'Dịch vụ di động đến tận nơi để sạc pin khẩn cấp khi xe hết pin giữa đường (trong phạm vi 20km).',
    800000,
    90,
    true,
    CURRENT_TIMESTAMP
),

-- Inspection & Diagnostic
(
    uuid_generate_v4(),
    'Kiểm tra tổng thể 100 điểm',
    'Kiểm tra tổng thể 100 điểm toàn bộ xe: pin, động cơ, phanh, treo, lốp, điện, điện tử, an toàn. Báo cáo chi tiết.',
    1500000,
    120,
    true,
    CURRENT_TIMESTAMP
),
(
    uuid_generate_v4(),
    'Chẩn đoán lỗi chuyên sâu',
    'Sử dụng thiết bị chẩn đoán chuyên dụng để quét và phân tích mã lỗi, kiểm tra chi tiết các hệ thống điện tử.',
    1000000,
    90,
    true,
    CURRENT_TIMESTAMP
),

-- Pre-owned Vehicle Services
(
    uuid_generate_v4(),
    'Kiểm tra xe cũ trước mua',
    'Dịch vụ kiểm tra toàn diện xe điện cũ trước khi mua: tình trạng pin, động cơ, khung xe, lịch sử bảo dưỡng, tình trạng pháp lý.',
    2000000,
    180,
    true,
    CURRENT_TIMESTAMP
);

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Service packages inserted successfully!';
    RAISE NOTICE '📦 Total packages: 36 service packages';
    RAISE NOTICE '🔋 Categories: Maintenance, Battery, Motor, Brakes, Tires, A/C, Charging, Electrical, Safety, Detailing, Emergency, Diagnostic';
END $$;
