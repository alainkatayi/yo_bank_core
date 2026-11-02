CREATE TABLE link_request (
    id              UUID PRIMARY KEY,
    account_number  CHAR(12)     NOT NULL,
    account_id      UUID         NULL REFERENCES bank_account(id),
    eligible        BOOLEAN      NOT NULL,
    code_hash       CHAR(64)     NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    status          VARCHAR(12)  NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    expires_at      TIMESTAMPTZ  NOT NULL,
    confirmed_at    TIMESTAMPTZ  NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_link_request_account_created ON link_request (account_number, created_at DESC);
