-- Migration: Add image_url column to vehicle_models table
-- Date: 2025-10-27

-- Step 1: Add image_url column
ALTER TABLE vehicle_models 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Step 2: Update existing vehicle models with image URLs
-- Các ảnh này nên được đặt trong thư mục public/assets/images/ của Frontend

-- VinFast Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vinfast-vf8.png'
WHERE manufacturer = 'VinFast' AND model = 'VF 8';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vinfast-vf9.png'
WHERE manufacturer = 'VinFast' AND model = 'VF 9';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vinfast-vf5.png'
WHERE manufacturer = 'VinFast' AND model = 'VF 5 Plus';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vinfast-e34.png'
WHERE manufacturer = 'VinFast' AND model = 'VF e34';

-- Tesla Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/tesla-model3.png'
WHERE manufacturer = 'Tesla' AND model LIKE 'Model 3%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/tesla-modely.png'
WHERE manufacturer = 'Tesla' AND model LIKE 'Model Y%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/tesla-models.png'
WHERE manufacturer = 'Tesla' AND model LIKE 'Model S%';

-- BYD Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/byd-atto3.png'
WHERE manufacturer = 'BYD' AND model = 'Atto 3';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/byd-seal.png'
WHERE manufacturer = 'BYD' AND model = 'Seal';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/byd-tang.png'
WHERE manufacturer = 'BYD' AND model = 'Tang EV';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/byd-dolphin.png'
WHERE manufacturer = 'BYD' AND model = 'Dolphin';

-- Hyundai Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/hyundai-ioniq5.png'
WHERE manufacturer = 'Hyundai' AND model = 'Ioniq 5';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/hyundai-ioniq6.png'
WHERE manufacturer = 'Hyundai' AND model = 'Ioniq 6';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/hyundai-kona.png'
WHERE manufacturer = 'Hyundai' AND model = 'Kona Electric';

-- Kia Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/kia-ev6.png'
WHERE manufacturer = 'Kia' AND model = 'EV6';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/kia-niro.png'
WHERE manufacturer = 'Kia' AND model = 'Niro EV';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/kia-ev9.png'
WHERE manufacturer = 'Kia' AND model = 'EV9';

-- Mercedes-Benz Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/mercedes-eqs.png'
WHERE manufacturer = 'Mercedes-Benz' AND model LIKE 'EQS%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/mercedes-eqe.png'
WHERE manufacturer = 'Mercedes-Benz' AND model LIKE 'EQE%';

-- BMW Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/bmw-ix.png'
WHERE manufacturer = 'BMW' AND model LIKE 'iX%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/bmw-i4.png'
WHERE manufacturer = 'BMW' AND model LIKE 'i4%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/bmw-ix3.png'
WHERE manufacturer = 'BMW' AND model = 'iX3';

-- Audi Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/audi-etron-gt.png'
WHERE manufacturer = 'Audi' AND model = 'e-tron GT';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/audi-q4.png'
WHERE manufacturer = 'Audi' AND model = 'Q4 e-tron';

-- Nissan Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/nissan-leaf.png'
WHERE manufacturer = 'Nissan' AND model LIKE 'Leaf%';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/nissan-ariya.png'
WHERE manufacturer = 'Nissan' AND model = 'Ariya';

-- Volkswagen Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vw-id4.png'
WHERE manufacturer = 'Volkswagen' AND model = 'ID.4';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/vw-id3.png'
WHERE manufacturer = 'Volkswagen' AND model = 'ID.3';

-- Porsche Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/porsche-taycan.png'
WHERE manufacturer = 'Porsche' AND model LIKE 'Taycan%';

-- MG Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/mg-zs.png'
WHERE manufacturer = 'MG' AND model = 'ZS EV';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/mg-mg4.png'
WHERE manufacturer = 'MG' AND model = 'MG4 Electric';

-- Polestar Models
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/polestar-2.png'
WHERE manufacturer = 'Polestar' AND model = 'Polestar 2';

UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/polestar-3.png'
WHERE manufacturer = 'Polestar' AND model = 'Polestar 3';

-- Default placeholder for models without specific images
UPDATE vehicle_models 
SET image_url = '/assets/images/vehicles/default-ev.png'
WHERE image_url IS NULL;

COMMIT;
