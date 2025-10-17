-- Fix foreign key constraint for service_orders.technician_id
-- Issue: Constraint is pointing to 'users' table instead of 'staff' table

-- 1. Drop the incorrect foreign key constraint
ALTER TABLE service_orders
DROP CONSTRAINT IF EXISTS fkta3246v142v6vtiyjguub1vd2;

-- 2. Add the correct foreign key constraint pointing to staff table
ALTER TABLE service_orders
ADD CONSTRAINT fk_service_orders_technician
FOREIGN KEY (technician_id)
REFERENCES staff(id)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Verify the constraint
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'service_orders'
    AND kcu.column_name = 'technician_id';

