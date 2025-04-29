CREATE SCHEMA IF NOT EXISTS mng;

CREATE TABLE mng.wallet (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    balance BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE SCHEMA IF NOT EXISTS txn;

CREATE TABLE txn.transaction (
    id UUID PRIMARY KEY,
    from_id UUID NOT NULL,
    to_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    FOREIGN KEY (from_id) REFERENCES mng.wallet(id),
    FOREIGN KEY (to_id) REFERENCES mng.wallet(id)
);

CREATE SCHEMA IF NOT EXISTS gnl;

CREATE TABLE gnl.ledger (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    running_balance BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (transaction_id, wallet_id),
    FOREIGN KEY (transaction_id) REFERENCES txn.transaction(id),
    FOREIGN KEY (wallet_id) REFERENCES mng.wallet(id)
);