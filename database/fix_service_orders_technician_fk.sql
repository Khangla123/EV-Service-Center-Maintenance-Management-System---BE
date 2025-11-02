-- Fix service_orders.technician_id foreign key constraint
-- Problem: Có 2 FK constraints trên cùng column technician_id
-- Solution: Drop constraint cũ (trỏ đến users), giữ lại constraint mới (trỏ đến staff)

-- Drop constraint cũ (trỏ đến users.id)
ALTER TABLE service_orders 
DROP CONSTRAINT IF EXISTS fkta3246v142v6vtiyjguub1vd2;

-- Verify: Check remaining constraints
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'service_orders'::regclass 
  AND contype = 'f' 
  AND conname LIKE '%technician%';

-- Expected result: Only "service_orders_technician_id_fkey" pointing to staff(id)
