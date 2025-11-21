-- ===================================================================
-- EV SERVICE CENTER MAINTENANCE MANAGEMENT SYSTEM
-- Complete Database Schema (PostgreSQL)
-- Generated: November 21, 2025
-- Description: Comprehensive schema without sample data
-- ===================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===================================================================
-- NOTE: Using VARCHAR instead of ENUMs for better JPA compatibility
-- All status/type constraints are enforced via CHECK constraints
-- ===================================================================

-- ===================================================================
-- CORE TABLES
-- ===================================================================

-- Service Centers Table
CREATE TABLE service_centers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    operating_hours JSONB, -- {"monday": "08:00-18:00", "tuesday": "08:00-18:00", ...}
    capacity INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(20) DEFAULT 'user', -- user, CUSTOMER, STAFF, TECHNICIAN, ADMIN
    full_name VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    last_login TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Customers Table (extends users for customers)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_code VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    subscription_expiry TIMESTAMP WITH TIME ZONE,
    total_spent DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Staff Table (extends users for staff/technicians)
CREATE TABLE staff (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    service_center_id UUID REFERENCES service_centers(id),
    staff_code VARCHAR(20) UNIQUE,
    specialization VARCHAR(255),
    hire_date DATE,
    salary DECIMAL(15,2),
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Vehicle Models Table
CREATE TABLE vehicle_models (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    manufacturer VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER,
    battery_capacity DECIMAL(8,2), -- kWh
    range_km INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Vehicles Table
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE,
    vehicle_model_id UUID REFERENCES vehicle_models(id),
    vin VARCHAR(17) UNIQUE NOT NULL,
    license_plate VARCHAR(20) UNIQUE,
    color VARCHAR(50),
    purchase_date DATE,
    warranty_expiration DATE,
    mileage INTEGER DEFAULT 0,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Packages Table
CREATE TABLE service_packages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    duration_minutes INTEGER, -- estimated duration
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Appointments Table (with all enhancements)
CREATE TABLE service_appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id),
    vehicle_id UUID REFERENCES vehicles(id),
    service_center_id UUID REFERENCES service_centers(id),
    service_package_id UUID REFERENCES service_packages(id),
    technician_id UUID REFERENCES staff(id) ON DELETE SET NULL,
    appointment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    selected_packages JSONB DEFAULT '[]'::jsonb,
    estimated_completion TIMESTAMP WITH TIME ZONE,
    actual_completion TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_appointment_status CHECK (status IN ('PENDING', 'CONFIRMED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

COMMENT ON COLUMN service_appointments.selected_packages IS 'JSON array storing multiple selected service packages with details (packageId, packageName, price, durationMinutes, description)';

-- Appointment Service Packages Junction Table
CREATE TABLE appointment_service_packages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID NOT NULL REFERENCES service_appointments(id) ON DELETE CASCADE,
    service_package_id UUID NOT NULL REFERENCES service_packages(id) ON DELETE CASCADE,
    price DECIMAL(10,2) NOT NULL, -- Price at time of booking (for historical accuracy)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Orders Table
CREATE TABLE service_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID REFERENCES service_appointments(id),
    order_code VARCHAR(20) UNIQUE,
    technician_id UUID REFERENCES users(id), -- References users.id (not staff.id)
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    checklist TEXT, -- JSON string for EV checklist items
    issues TEXT, -- JSON string for detected issues/problems
    diagnosis TEXT,
    work_performed TEXT,
    total_amount DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Parts/Inventory Table
CREATE TABLE parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_center_id UUID REFERENCES service_centers(id),
    part_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    unit_price DECIMAL(10,2),
    stock_quantity INTEGER DEFAULT 0,
    min_stock_level INTEGER DEFAULT 0,
    supplier VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Order Parts (Many-to-Many)
CREATE TABLE service_order_parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID REFERENCES service_orders(id) ON DELETE CASCADE,
    part_id UUID REFERENCES parts(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Suggestions Table
CREATE TABLE service_suggestions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    service_name VARCHAR(255) NOT NULL,
    reason TEXT, -- Lý do đề xuất
    estimated_cost DECIMAL(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_suggestion_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Invoices Table
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID REFERENCES service_orders(id),
    customer_id UUID REFERENCES customers(id),
    invoice_number VARCHAR(50) UNIQUE,
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE, CANCELLED
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_invoice_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED'))
);

-- Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID REFERENCES invoices(id),
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- CASH, CARD, E_WALLET, BANKING
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, FAILED, REFUNDED
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP WITH TIME ZONE,
    reference_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'E_WALLET', 'BANKING')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))
);

-- Notifications Table (with all enhancements)
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    appointment_id UUID REFERENCES service_appointments(id) ON DELETE SET NULL,
    invoice_id UUID REFERENCES invoices(id) ON DELETE SET NULL,
    type VARCHAR(50) NOT NULL, -- APPOINTMENT_CREATED_BY_STAFF, INVOICE_CREATED
    title VARCHAR(255) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_notification_type CHECK (type IN ('APPOINTMENT_CREATED_BY_STAFF', 'INVOICE_CREATED'))
);

COMMENT ON COLUMN notifications.appointment_id IS 'Reference to appointment created by staff for customer';
COMMENT ON COLUMN notifications.invoice_id IS 'Reference to invoice created for customer';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when customer marked notification as read';

-- Password Reset Tokens Table
CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(255),
    email VARCHAR(255),
    otp_code VARCHAR(10),
    expiry_time TIMESTAMP WITH TIME ZONE,
    verified BOOLEAN DEFAULT false
);

-- Chat Messages Table
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID REFERENCES service_orders(id),
    sender_id UUID REFERENCES users(id),
    message TEXT NOT NULL,
    is_staff BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Maintenance Plans Table
CREATE TABLE maintenance_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vehicle_model_id UUID REFERENCES vehicle_models(id),
    service_package_id UUID REFERENCES service_packages(id),
    interval_km INTEGER, -- every X kilometers
    interval_months INTEGER, -- every X months
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Certifications Table
CREATE TABLE certifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id UUID REFERENCES staff(id),
    name VARCHAR(255) NOT NULL,
    issuing_authority VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    certificate_number VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Work Schedules Table
CREATE TABLE work_schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id UUID REFERENCES staff(id),
    date DATE NOT NULL,
    shift_start TIME NOT NULL,
    shift_end TIME NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ===================================================================

-- Users & Authentication
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- Vehicles
CREATE INDEX idx_vehicles_customer_id ON vehicles(customer_id);
CREATE INDEX idx_vehicles_vin ON vehicles(vin);

-- Service Appointments
CREATE INDEX idx_appointments_customer_id ON service_appointments(customer_id);
CREATE INDEX idx_appointments_date ON service_appointments(appointment_date);
CREATE INDEX idx_appointments_status ON service_appointments(status);
CREATE INDEX idx_appointments_technician ON service_appointments(technician_id);
CREATE INDEX idx_appointments_status_technician ON service_appointments(status, technician_id);
CREATE INDEX idx_service_appointments_selected_packages ON service_appointments USING GIN (selected_packages);

-- Appointment Service Packages
CREATE INDEX idx_appointment_packages_appointment ON appointment_service_packages(appointment_id);
CREATE INDEX idx_appointment_packages_package ON appointment_service_packages(service_package_id);

-- Service Orders
CREATE INDEX idx_orders_appointment_id ON service_orders(appointment_id);
CREATE INDEX idx_orders_technician_id ON service_orders(technician_id);

-- Parts & Inventory
CREATE INDEX idx_parts_service_center_id ON parts(service_center_id);
CREATE INDEX idx_parts_stock ON parts(stock_quantity, min_stock_level);

-- Service Suggestions
CREATE INDEX idx_suggestion_service_order ON service_suggestions(service_order_id);
CREATE INDEX idx_suggestion_status ON service_suggestions(status);

-- Notifications
CREATE INDEX idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_appointment_id ON notifications(appointment_id);
CREATE INDEX idx_notifications_invoice_id ON notifications(invoice_id);

-- Password Reset Tokens
CREATE INDEX idx_password_reset_email ON password_reset_token(email);
CREATE INDEX idx_password_reset_token ON password_reset_token(token);

-- Chat Messages
CREATE INDEX idx_chat_service_order ON chat_messages(service_order_id);

-- ===================================================================
-- TRIGGERS FOR AUTO-UPDATE
-- ===================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tables with updated_at column
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at 
    BEFORE UPDATE ON vehicles 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_appointments_updated_at 
    BEFORE UPDATE ON service_appointments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at 
    BEFORE UPDATE ON service_orders 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_parts_updated_at 
    BEFORE UPDATE ON parts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_centers_updated_at 
    BEFORE UPDATE ON service_centers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_update_service_suggestions_updated_at
    BEFORE UPDATE ON service_suggestions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===================================================================
-- ROW LEVEL SECURITY (RLS) - Optional, not used with JPA
-- ===================================================================
-- Uncomment if needed:
-- ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE vehicles ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE service_appointments ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- ===================================================================
-- VIEWS FOR REPORTING AND ANALYTICS
-- ===================================================================

-- View: Appointment Details with Full Information
CREATE OR REPLACE VIEW vw_appointment_details AS
SELECT 
    sa.id AS appointment_id,
    sa.appointment_date,
    sa.status,
    u.full_name AS customer_name,
    u.email AS customer_email,
    u.phone AS customer_phone,
    v.license_plate,
    vm.manufacturer || ' ' || vm.model AS vehicle_info,
    sp.name AS service_package_name,
    sp.price AS service_price,
    sa.selected_packages,
    sc.name AS service_center_name,
    s_tech.full_name AS technician_name,
    sa.created_at
FROM service_appointments sa
JOIN customers c ON sa.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicles v ON sa.vehicle_id = v.id
JOIN vehicle_models vm ON v.vehicle_model_id = vm.id
LEFT JOIN service_packages sp ON sa.service_package_id = sp.id
LEFT JOIN service_centers sc ON sa.service_center_id = sc.id
LEFT JOIN staff st ON sa.technician_id = st.id
LEFT JOIN users s_tech ON st.user_id = s_tech.id;

-- View: Vehicle Maintenance History
CREATE OR REPLACE VIEW vw_vehicle_maintenance_history AS
SELECT 
    v.id AS vehicle_id,
    v.license_plate,
    vm.manufacturer || ' ' || vm.model AS vehicle_info,
    sa.appointment_date,
    sa.status,
    sp.name AS service_performed,
    so.total_amount,
    u.full_name AS technician_name
FROM vehicles v
JOIN vehicle_models vm ON v.vehicle_model_id = vm.id
LEFT JOIN service_appointments sa ON v.id = sa.vehicle_id
LEFT JOIN service_packages sp ON sa.service_package_id = sp.id
LEFT JOIN service_orders so ON sa.id = so.appointment_id
LEFT JOIN staff st ON so.technician_id = st.id
LEFT JOIN users u ON st.user_id = u.id
WHERE sa.status IN ('COMPLETED', 'IN_PROGRESS')
ORDER BY sa.appointment_date DESC;

-- View: Revenue Summary
CREATE OR REPLACE VIEW vw_revenue_summary AS
SELECT 
    DATE_TRUNC('month', i.issued_at) AS month,
    COUNT(i.id) AS total_invoices,
    SUM(i.total_amount) AS total_revenue,
    SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE 0 END) AS paid_revenue,
    SUM(CASE WHEN p.status = 'PENDING' THEN i.total_amount ELSE 0 END) AS pending_revenue
FROM invoices i
LEFT JOIN payments p ON i.id = p.invoice_id
GROUP BY DATE_TRUNC('month', i.issued_at)
ORDER BY month DESC;

-- View: Staff Workload
CREATE OR REPLACE VIEW vw_staff_workload AS
SELECT 
    s.id AS staff_id,
    u.full_name AS staff_name,
    s.specialization,
    COUNT(CASE WHEN sa.status = 'IN_PROGRESS' THEN 1 END) AS active_appointments,
    COUNT(CASE WHEN sa.status = 'COMPLETED' THEN 1 END) AS completed_appointments,
    ROUND(AVG(so.total_amount), 2) AS avg_order_value
FROM staff s
JOIN users u ON s.user_id = u.id
LEFT JOIN service_appointments sa ON s.id = sa.technician_id
LEFT JOIN service_orders so ON sa.id = so.appointment_id
GROUP BY s.id, u.full_name, s.specialization;

-- ===================================================================
-- UTILITY FUNCTIONS
-- ===================================================================

-- Function: Calculate next maintenance date
CREATE OR REPLACE FUNCTION calculate_next_maintenance_date(
    p_vehicle_id UUID,
    p_interval_months INTEGER DEFAULT 6
) RETURNS DATE AS $$
DECLARE
    v_last_maintenance DATE;
    v_next_maintenance DATE;
BEGIN
    SELECT last_maintenance_date INTO v_last_maintenance
    FROM vehicles WHERE id = p_vehicle_id;
    
    IF v_last_maintenance IS NULL THEN
        v_next_maintenance := CURRENT_DATE + INTERVAL '1 month' * p_interval_months;
    ELSE
        v_next_maintenance := v_last_maintenance + INTERVAL '1 month' * p_interval_months;
    END IF;
    
    RETURN v_next_maintenance;
END;
$$ LANGUAGE plpgsql;

-- Function: Check parts inventory low stock
CREATE OR REPLACE FUNCTION check_low_stock_parts()
RETURNS TABLE (
    part_id UUID,
    part_name VARCHAR(255),
    current_stock INTEGER,
    min_level INTEGER,
    shortage INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.stock_quantity,
        p.min_stock_level,
        (p.min_stock_level - p.stock_quantity) AS shortage
    FROM parts p
    WHERE p.stock_quantity <= p.min_stock_level
    ORDER BY shortage DESC;
END;
$$ LANGUAGE plpgsql;

-- ===================================================================
-- COMMENTS FOR DOCUMENTATION
-- ===================================================================

COMMENT ON TABLE service_centers IS 'EV service center locations';
COMMENT ON TABLE users IS 'All system users (customers, staff, technicians, admins)';
COMMENT ON TABLE customers IS 'Customer-specific information linked to users';
COMMENT ON TABLE staff IS 'Staff and technician information';
COMMENT ON TABLE vehicles IS 'Customer-owned electric vehicles';
COMMENT ON TABLE service_appointments IS 'Service appointments scheduled by customers';
COMMENT ON TABLE service_orders IS 'Actual service work orders performed by technicians';
COMMENT ON TABLE service_suggestions IS 'Additional service recommendations from technicians';
COMMENT ON TABLE parts IS 'Parts inventory for repairs and maintenance';
COMMENT ON TABLE invoices IS 'Billing invoices for completed services';
COMMENT ON TABLE payments IS 'Payment transactions for invoices';
COMMENT ON TABLE notifications IS 'Customer notifications for appointments, reminders, etc.';

-- ===================================================================
-- END OF SCHEMA
-- ===================================================================

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Database schema created successfully!';
    RAISE NOTICE '📊 Total tables created: 20+';
    RAISE NOTICE '🔍 Indexes optimized for performance';
    RAISE NOTICE '📈 Views and functions ready for analytics';
END $$;
