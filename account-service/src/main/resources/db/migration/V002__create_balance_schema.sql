CREATE TABLE balances (
                      account_id UUID PRIMARY KEY,
                      amount NUMERIC(19,4) NOT NULL,
                      updated_at TIMESTAMP NOT NULL DEFAULT now(),
                      CONSTRAINT fk_balance_account
                          FOREIGN KEY (account_id)
                          REFERENCES accounts(account_id)
);