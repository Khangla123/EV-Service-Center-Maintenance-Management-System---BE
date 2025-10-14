-- Migration: Add warranty_expiration column to vehicles table

-- Step 1: Add column (nullable first to avoid issues with existing data)
ALTER TABLE vehicles 
ADD COLUMN warranty_expiration DATE NULL;

-- Step 2: Update existing vehicles with calculated warranty (purchase_date + 3 years)
UPDATE vehicles 
SET warranty_expiration = DATE_ADD(purchase_date, INTERVAL 3 YEAR)
WHERE purchase_date IS NOT NULL 
  AND warranty_expiration IS NULL;

-- Step 3: For vehicles without purchase_date, set a default (optional)
UPDATE vehicles 
SET warranty_expiration = DATE_ADD(NOW(), INTERVAL 3 YEAR)
WHERE warranty_expiration IS NULL;

-- Verify the changes
SELECT 
    id,
    vin,
    license_plate,
    purchase_date,
    warranty_expiration,
    DATEDIFF(warranty_expiration, purchase_date) / 365 as warranty_years
FROM vehicles 
LIMIT 10;
