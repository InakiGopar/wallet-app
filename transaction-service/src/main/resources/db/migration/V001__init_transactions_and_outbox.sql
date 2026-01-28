CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              account_id UUID NOT NULL,

                              amount NUMERIC(19, 4) NOT NULL,
                              currency VARCHAR(3) NOT NULL,

                              type VARCHAR(20) NOT NULL,
                              status VARCHAR(20) NOT NULL,

                              created_at TIMESTAMP WITH TIME ZONE NOT NULL
);


CREATE TABLE outbox_event (
                              id UUID PRIMARY KEY,
                              aggregate_id UUID NOT NULL,
                              aggregate_type VARCHAR(50) NOT NULL,
                              type VARCHAR(100) NOT NULL,
                              payload TEXT NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              occurred_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbox_status
    ON outbox_event (status);