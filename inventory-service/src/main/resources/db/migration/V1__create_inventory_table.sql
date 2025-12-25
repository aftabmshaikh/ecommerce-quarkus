-- Create inventory_items table
CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    sku_code VARCHAR(255) NOT NULL UNIQUE,
    quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL,
    low_stock_threshold INTEGER NOT NULL,
    restock_threshold INTEGER NOT NULL,
    last_restocked_date TIMESTAMP,
    next_restock_date TIMESTAMP,
    unit_cost NUMERIC(19, 4),
    total_value NUMERIC(19, 4),
    location_code VARCHAR(255),
    bin_location VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_inventory_items_product_id ON inventory_items(product_id);
CREATE INDEX idx_inventory_items_sku_code ON inventory_items(sku_code);
CREATE INDEX idx_inventory_items_is_active ON inventory_items(is_active);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to update the updated_at column
CREATE TRIGGER update_inventory_items_updated_at
BEFORE UPDATE ON inventory_items
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
