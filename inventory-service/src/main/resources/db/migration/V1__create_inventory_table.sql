-- Create inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    stock_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

-- Create index for product_id
CREATE INDEX idx_inventory_product_id ON inventory(product_id);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to update the updated_at column
CREATE TRIGGER update_inventory_updated_at
BEFORE UPDATE ON inventory
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to calculate available_quantity
CREATE OR REPLACE FUNCTION calculate_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity := NEW.stock_quantity - NEW.reserved_quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to calculate available_quantity before insert or update
CREATE TRIGGER calculate_available_quantity_trigger
BEFORE INSERT OR UPDATE OF stock_quantity, reserved_quantity ON inventory
FOR EACH ROW EXECUTE FUNCTION calculate_available_quantity();
