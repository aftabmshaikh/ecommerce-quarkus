-- Create enum type for order status (keeping for reference, but using VARCHAR for compatibility)
-- CREATE TYPE order_status AS ENUM (
--     'PENDING',
--     'PROCESSING',
--     'PAID',
--     'SHIPPED',
--     'DELIVERED',
--     'CANCELLED',
--     'REFUNDED'
-- );

-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    shipping_address TEXT NOT NULL,
    billing_address TEXT NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    tax DECIMAL(19, 4) NOT NULL,
    shipping_fee DECIMAL(19, 4) NOT NULL,
    total DECIMAL(19, 4) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    processing_at TIMESTAMP,
    shipped_at TIMESTAMP,
    out_for_delivery_at TIMESTAMP,
    delivered_date TIMESTAMP,
    cancelled_at TIMESTAMP,
    return_requested_at TIMESTAMP,
    returned_at TIMESTAMP,
    estimated_delivery_date TIMESTAMP,
    tracking_number VARCHAR(255),
    carrier VARCHAR(100),
    currency VARCHAR(10),
    version BIGINT NOT NULL
);

-- Create order_items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_price DECIMAL(19, 4) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create order_status_history table
CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    status_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for automatic timestamp updates
CREATE TRIGGER update_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at
BEFORE UPDATE ON order_items
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create a function to calculate total price for order items
CREATE OR REPLACE FUNCTION calculate_order_item_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total_price = NEW.unit_price * NEW.quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to calculate total price before insert or update
CREATE TRIGGER calculate_order_item_total_trigger
BEFORE INSERT OR UPDATE OF unit_price, quantity ON order_items
FOR EACH ROW EXECUTE FUNCTION calculate_order_item_total();

-- Create a function to validate order status transitions
CREATE OR REPLACE FUNCTION validate_order_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- Allow status to stay the same (for updates that don't change status)
    IF OLD.status = NEW.status THEN
        RETURN NEW;
    END IF;
    
    -- Define valid status transitions
    IF (OLD.status = 'PENDING' AND NEW.status IN ('PROCESSING', 'CANCELLED')) OR
       (OLD.status = 'PROCESSING' AND NEW.status IN ('PAID', 'CANCELLED')) OR
       (OLD.status = 'PAID' AND NEW.status IN ('SHIPPED', 'CANCELLED')) OR
       (OLD.status = 'SHIPPED' AND NEW.status IN ('DELIVERED', 'CANCELLED')) OR
       (OLD.status = 'DELIVERED' AND NEW.status = 'REFUNDED') THEN
        RETURN NEW;
    END IF;
    
    RAISE EXCEPTION 'Invalid status transition from % to %', OLD.status, NEW.status;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to validate status transitions
CREATE TRIGGER validate_order_status_transition_trigger
BEFORE UPDATE OF status ON orders
FOR EACH ROW EXECUTE FUNCTION validate_order_status_transition();
