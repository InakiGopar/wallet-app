-- accounts
ALTER TABLE accounts
    ALTER COLUMN created_at DROP DEFAULT,
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
        USING created_at AT TIME ZONE 'UTC';

-- balances
ALTER TABLE balances
    ALTER COLUMN updated_at DROP DEFAULT,
ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE
        USING updated_at AT TIME ZONE 'UTC';

-- outbox_event
ALTER TABLE outbox_event
    ALTER COLUMN created_at DROP DEFAULT,
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
        USING created_at AT TIME ZONE 'UTC';

ALTER TABLE outbox_event
ALTER COLUMN occurred_at TYPE TIMESTAMP WITH TIME ZONE
        USING occurred_at AT TIME ZONE 'UTC';