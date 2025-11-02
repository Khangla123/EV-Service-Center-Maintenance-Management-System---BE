-- Fix user roles to match PostgreSQL ENUM values (UPPERCASE)
-- Run this to fix existing data with lowercase or "string" role values

-- Update lowercase roles to UPPERCASE
UPDATE users SET role = 'STAFF' WHERE role = 'staff';
UPDATE users SET role = 'TECHNICIAN' WHERE role = 'technician';
UPDATE users SET role = 'CUSTOMER' WHERE role = 'customer';
UPDATE users SET role = 'ADMIN' WHERE role = 'admin';

-- Fix any "string" literal roles to appropriate role based on staff table
UPDATE users u
SET role = CASE 
    WHEN EXISTS (SELECT 1 FROM staff s WHERE s.user_id = u.id AND s.specialization IS NOT NULL AND s.specialization != '') 
        THEN 'TECHNICIAN'
    WHEN EXISTS (SELECT 1 FROM staff s WHERE s.user_id = u.id) 
        THEN 'STAFF'
    ELSE 'CUSTOMER'
END
WHERE u.role = 'string' OR u.role NOT IN ('CUSTOMER', 'STAFF', 'TECHNICIAN', 'ADMIN');

-- Verify the fix
SELECT id, email, role, full_name FROM users ORDER BY role;
