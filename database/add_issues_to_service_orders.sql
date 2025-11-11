-- Add issues column to service_orders table
-- Issues will store detected problems during inspection

ALTER TABLE service_orders 
ADD COLUMN IF NOT EXISTS issues JSONB DEFAULT '[]'::jsonb;

COMMENT ON COLUMN service_orders.issues IS 'List of issues/problems detected by technician during inspection. Format: [{issue, severity, recommendation}]';

-- Example data structure:
-- [
--   {
--     "issue": "Má phanh mòn 80%",
--     "severity": "high",
--     "recommendation": "Cần thay ngay",
--     "detectedAt": "2025-11-11T10:30:00Z"
--   },
--   {
--     "issue": "Pin sạc chậm",
--     "severity": "medium", 
--     "recommendation": "Kiểm tra cáp sạc",
--     "detectedAt": "2025-11-11T10:35:00Z"
--   }
-- ]
