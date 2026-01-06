CREATE TABLE outbox_event (
                              id UUID PRIMARY KEY,
                              aggregate_id UUID NOT NULL,
                              aggregate_type VARCHAR(50) NOT NULL,
                              type VARCHAR(100) NOT NULL,
                              payload TEXT NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              occurred_at TIMESTAMP NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_status
    ON outbox_event (status);
