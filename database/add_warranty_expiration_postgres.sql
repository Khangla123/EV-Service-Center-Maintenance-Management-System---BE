-- Migration: Add warranty_expiration column to vehicles table (PostgreSQL)

-- Step 1: Add column (nullable first to avoid issues with existing data)
ALTER TABLE vehicles 
ADD COLUMN warranty_expiration DATE NULL;

-- Step 2: Update existing vehicles with calculated warranty (purchase_date + 3 years)
UPDATE vehicles 
SET warranty_expiration = purchase_date + INTERVAL '3 years'
WHERE purchase_date IS NOT NULL 
  AND warranty_expiration IS NULL;

-- Step 3: For vehicles without purchase_date, set a default (optional)
UPDATE vehicles 
SET warranty_expiration = CURRENT_DATE + INTERVAL '3 years'
WHERE warranty_expiration IS NULL;

-- Step 4: Verify the changes
SELECT 
    id,
    vin,
    license_plate,
    purchase_date,
    warranty_expiration,
    (warranty_expiration - purchase_date) as warranty_days
FROM vehicles 
LIMIT 10;

-- Optional: Add index for better query performance
CREATE INDEX idx_vehicles_warranty_expiration ON vehicles(warranty_expiration);

-- Optional: Add comment
COMMENT ON COLUMN vehicles.warranty_expiration IS 'Vehicle warranty expiration date, typically 3 years from purchase date';
