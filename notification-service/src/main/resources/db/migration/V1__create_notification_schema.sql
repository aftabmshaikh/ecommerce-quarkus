CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_sent ON notifications(sent);
