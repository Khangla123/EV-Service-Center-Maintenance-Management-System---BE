-- EV Service Center Maintenance Management System Database
-- PostgreSQL Schema with Security & Optimization

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Enums
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'STAFF', 'TECHNICIAN', 'ADMIN');
CREATE TYPE appointment_status AS ENUM ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE service_status AS ENUM ('WAITING', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'REFUNDED');
CREATE TYPE payment_method AS ENUM ('CASH', 'CARD', 'E_WALLET', 'BANKING');
CREATE TYPE notification_type AS ENUM ('MAINTENANCE_REMINDER', 'PAYMENT_REMINDER', 'APPOINTMENT_UPDATE', 'SERVICE_COMPLETE');

-- Service Centers Table (Added)
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
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role user_role NOT NULL DEFAULT 'CUSTOMER',
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
                          mileage INTEGER DEFAULT 0,
                          last_maintenance_date DATE,
                          next_maintenance_date DATE,
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

-- Service Appointments Table
CREATE TABLE service_appointments (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      customer_id UUID REFERENCES customers(id),
                                      vehicle_id UUID REFERENCES vehicles(id),
                                      service_center_id UUID REFERENCES service_centers(id),
                                      service_package_id UUID REFERENCES service_packages(id),
                                      appointment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status appointment_status DEFAULT 'PENDING',
    notes TEXT,
    estimated_completion TIMESTAMP WITH TIME ZONE,
    actual_completion TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Service Orders Table
CREATE TABLE service_orders (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                appointment_id UUID REFERENCES service_appointments(id),
                                order_code VARCHAR(20) UNIQUE,
                                technician_id UUID REFERENCES staff(id),
                                status service_status DEFAULT 'WAITING',
                                start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    checklist JSONB, -- EV checklist items
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
                          issued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Payments Table
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          invoice_id UUID REFERENCES invoices(id),
                          amount DECIMAL(15,2) NOT NULL,
                          payment_method payment_method NOT NULL,
                          status payment_status DEFAULT 'PENDING',
                          transaction_id VARCHAR(255),
                          payment_date TIMESTAMP WITH TIME ZONE,
    reference_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Notifications Table
CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               customer_id UUID REFERENCES customers(id),
                               type notification_type NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT false,
                               scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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

-- Indexes for Performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_vehicles_customer_id ON vehicles(customer_id);
CREATE INDEX idx_vehicles_vin ON vehicles(vin);
CREATE INDEX idx_appointments_customer_id ON service_appointments(customer_id);
CREATE INDEX idx_appointments_date ON service_appointments(appointment_date);
CREATE INDEX idx_appointments_status ON service_appointments(status);
CREATE INDEX idx_orders_appointment_id ON service_orders(appointment_id);
CREATE INDEX idx_orders_technician_id ON service_orders(technician_id);
CREATE INDEX idx_parts_service_center_id ON parts(service_center_id);
CREATE INDEX idx_parts_stock ON parts(stock_quantity, min_stock_level);
CREATE INDEX idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_at);
CREATE INDEX idx_chat_service_order ON chat_messages(service_order_id);

-- Update Triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_appointments_updated_at BEFORE UPDATE ON service_appointments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON service_orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_parts_updated_at BEFORE UPDATE ON parts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_service_centers_updated_at BEFORE UPDATE ON service_centers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Security: Row Level Security (RLS)
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- RLS Policies (Examples)
CREATE POLICY customer_own_data ON customers FOR ALL USING (user_id = current_setting('app.current_user_id')::uuid);
CREATE POLICY customer_own_vehicles ON vehicles FOR ALL USING (customer_id IN (SELECT id FROM customers WHERE user_id = current_setting('app.current_user_id')::uuid));