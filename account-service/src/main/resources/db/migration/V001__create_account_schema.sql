CREATE TABLE accounts (
                      account_id UUID PRIMARY KEY,
                      currency CHAR(3) NOT NULL,
                      status TEXT NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT now()
);
