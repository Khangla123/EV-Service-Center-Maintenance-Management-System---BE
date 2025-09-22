-- Connect to SQL Server as Administrator
-- Run this script to create database and setup user

-- Enable SQL Server Authentication (if needed)
USE master;
GO

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'EVServiceCenter')
    CREATE DATABASE EVServiceCenter;
GO

-- Create login for application (Optional - if you want dedicated user)
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = N'evservice_user')
BEGIN
    CREATE LOGIN evservice_user WITH PASSWORD = N'EVService123!@#';
END
GO

-- Use the database
USE EVServiceCenter;
GO

-- Create user for the login
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = N'evservice_user')
BEGIN
    CREATE USER evservice_user FOR LOGIN evservice_user;
    ALTER ROLE db_owner ADD MEMBER evservice_user;
END
GO

-- Create tables
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
END
GO

-- Create index on email for faster lookup
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='IX_users_email' AND object_id = OBJECT_ID('users'))
    CREATE INDEX IX_users_email ON users(email);
GO

-- Insert sample data only if table is empty
IF NOT EXISTS (SELECT 1 FROM users)
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone, role) VALUES
    ('admin@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Admin', 'User', '0123456789', 'ADMIN'),
    ('customer@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Customer', 'User', '0987654321', 'CUSTOMER'),
    ('staff@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Staff', 'User', '0111222333', 'STAFF'),
    ('technician@evservice.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMye6pD6YONmeJPZx3DVbRa8G5vQzOOH4K.', 'Technician', 'User', '0444555666', 'TECHNICIAN');
END
GO

-- Note: Password for all test accounts is "123456"
PRINT 'Database setup completed successfully!';