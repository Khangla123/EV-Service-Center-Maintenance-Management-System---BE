-- Migration: Add technician_id to service_appointments table
-- Date: 2025-10-26
-- Description: Add support for assigning technicians to appointments

-- Step 1: Add technician_id column
ALTER TABLE service_appointments 
ADD COLUMN technician_id UUID;

-- Step 2: Add foreign key constraint
ALTER TABLE service_appointments
ADD CONSTRAINT fk_appointment_technician
FOREIGN KEY (technician_id) 
REFERENCES staff(id)
ON DELETE SET NULL;

-- Step 3: Create index for better query performance
CREATE INDEX idx_appointments_technician 
ON service_appointments(technician_id);

-- Step 4: Create index for status + technician queries
CREATE INDEX idx_appointments_status_technician 
ON service_appointments(status, technician_id);

-- Optional: Update existing CONFIRMED appointments with notes containing technician info
-- (This is for migrating old data that used notes to store technician info)
-- Uncomment if needed:
-- UPDATE service_appointments sa
-- SET technician_id = (
--     SELECT s.id 
--     FROM staff s 
--     WHERE sa.notes LIKE CONCAT('%Technician:%', s.id, '%')
--     LIMIT 1
-- )
-- WHERE sa.status = 'CONFIRMED' 
-- AND sa.notes LIKE '%Technician:%'
-- AND sa.technician_id IS NULL;

-- Verify the changes
SELECT 
    COUNT(*) as total_appointments,
    COUNT(technician_id) as assigned_appointments,
    COUNT(*) - COUNT(technician_id) as unassigned_appointments
FROM service_appointments;

COMMIT;
