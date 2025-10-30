-- Add status column to invoices table
ALTER TABLE invoices 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';

-- Update existing invoices to PENDING status
UPDATE invoices 
SET status = 'PENDING' 
WHERE status IS NULL;

-- Add comment to explain the column
COMMENT ON COLUMN invoices.status IS 'Invoice payment status: PENDING, PAID, OVERDUE, CANCELLED';
