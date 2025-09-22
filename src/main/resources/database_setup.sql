-- Create database
CREATE DATABASE EVServiceCenter;
GO

USE EVServiceCenter;
GO

-- Create users table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(255) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    first_name NVARCHAR(255),
    last_name NVARCHAR(255),
    phone NVARCHAR(20),
    role NVARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    avatar NVARCHAR(500),
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 DEFAULT CURRENT_TIMESTAMP
);
GO

-- Create index on email for faster lookup
CREATE INDEX IX_users_email ON users(email);
GO

-- Insert sample data
INSERT INTO users (email, password, first_name, last_name, phone, role) VALUES
('admin@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Admin', 'User', '0123456789', 'ADMIN'),
('customer@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Customer', 'User', '0987654321', 'CUSTOMER'),
('staff@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Staff', 'User', '0111222333', 'STAFF'),
('technician@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Technician', 'User', '0444555666', 'TECHNICIAN');
GO

-- Note: Password for all test accounts is "123456"