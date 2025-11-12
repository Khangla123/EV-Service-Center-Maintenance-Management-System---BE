-- Create service_suggestions table for PostgreSQL
-- This table stores service suggestions from technicians when they find additional issues

CREATE TABLE IF NOT EXISTS service_suggestions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    reason TEXT,
    estimated_cost DECIMAL(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_suggestion_service_order FOREIGN KEY (service_order_id) 
        REFERENCES service_orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_suggestion_service_order ON service_suggestions(service_order_id);
CREATE INDEX IF NOT EXISTS idx_suggestion_status ON service_suggestions(status);

-- Create trigger to auto-update updated_at
CREATE OR REPLACE FUNCTION update_service_suggestions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_service_suggestions_updated_at
    BEFORE UPDATE ON service_suggestions
    FOR EACH ROW
    EXECUTE FUNCTION update_service_suggestions_updated_at();

-- Verify table creation
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'service_suggestions';
