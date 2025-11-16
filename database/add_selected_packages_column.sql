-- Migration Script: Add selected_packages column to service_appointments table
-- Purpose: Allow multiple service packages selection in JSON format
-- Date: 2025-11-13

-- Step 1: Add selected_packages column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'service_appointments'
        AND column_name = 'selected_packages'
    ) THEN
        ALTER TABLE service_appointments
        ADD COLUMN selected_packages JSONB DEFAULT '[]'::jsonb;

        RAISE NOTICE 'Column selected_packages has been added successfully';
    ELSE
        RAISE NOTICE 'Column selected_packages already exists, skipping...';
    END IF;
END $$;

-- Step 2: Add comment to the column
COMMENT ON COLUMN service_appointments.selected_packages IS 'JSON array storing multiple selected service packages with details (packageId, packageName, price, durationMinutes, description)';

-- Step 3: Create index for better JSON query performance
CREATE INDEX IF NOT EXISTS idx_service_appointments_selected_packages
ON service_appointments USING GIN (selected_packages);

-- Step 4: Migrate existing data from service_package_id to selected_packages
-- Only update records where selected_packages is NULL or empty
UPDATE service_appointments sa
SET selected_packages = jsonb_build_array(
    jsonb_build_object(
        'packageId', sa.service_package_id::text,
        'packageName', sp.name,
        'price', sp.price,
        'durationMinutes', sp.duration_minutes,
        'description', sp.description
    )
)
FROM service_packages sp
WHERE sa.service_package_id = sp.id
AND sa.service_package_id IS NOT NULL
AND (sa.selected_packages IS NULL OR sa.selected_packages = '[]'::jsonb);

-- Step 5: Display migration summary
DO $$
DECLARE
    total_records INT;
    updated_records INT;
    null_package_records INT;
BEGIN
    SELECT COUNT(*) INTO total_records FROM service_appointments;
    SELECT COUNT(*) INTO updated_records
    FROM service_appointments
    WHERE selected_packages IS NOT NULL AND selected_packages != '[]'::jsonb;
    SELECT COUNT(*) INTO null_package_records
    FROM service_appointments
    WHERE service_package_id IS NULL;

    RAISE NOTICE '======================================';
    RAISE NOTICE 'Migration Summary:';
    RAISE NOTICE '======================================';
    RAISE NOTICE 'Total appointments: %', total_records;
    RAISE NOTICE 'Updated with selected_packages: %', updated_records;
    RAISE NOTICE 'Appointments without package: %', null_package_records;
    RAISE NOTICE '======================================';
END $$;

-- Verification query (uncomment to run)
-- SELECT
--     id,
--     service_package_id,
--     selected_packages,
--     appointment_date
-- FROM service_appointments
-- ORDER BY created_at DESC
-- LIMIT 10;

