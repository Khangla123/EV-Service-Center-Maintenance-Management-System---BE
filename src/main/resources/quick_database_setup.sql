-- Quick setup for SQL Server
-- Run this in SQL Server Management Studio

-- Create database if not exists
USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'EVServiceCenter')
    CREATE DATABASE EVServiceCenter;
GO

USE EVServiceCenter;
GO

-- Create users table if not exists
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' and xtype='U')
BEGIN
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
    
    -- Create index
    CREATE INDEX IX_users_email ON users(email);
    
    -- Insert sample data
    INSERT INTO users (email, password, first_name, last_name, phone, role) VALUES
    ('admin@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Admin', 'User', '0123456789', 'ADMIN'),
    ('customer@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Customer', 'User', '0987654321', 'CUSTOMER'),
    ('staff@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Staff', 'User', '0111222333', 'STAFF'),
    ('technician@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Technician', 'User', '0444555666', 'TECHNICIAN');
    
    PRINT 'Database and users table created successfully!';
END
ELSE
    PRINT 'Users table already exists.';
GO

-- Test query
SELECT COUNT(*) as UserCount FROM users;
GO