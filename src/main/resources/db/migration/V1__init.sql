CREATE TABLE customer (
                          id          UUID PRIMARY KEY,
                          first_name  VARCHAR(100) NOT NULL,
                          last_name   VARCHAR(100) NOT NULL,
                          phone       VARCHAR(20)  NOT NULL UNIQUE,
                          email       VARCHAR(150),
                          status      VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE','SUSPENDED')),
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE bank_account (
                              id              UUID PRIMARY KEY,
                              account_number  CHAR(12)      NOT NULL UNIQUE,
                              customer_id     UUID          NOT NULL REFERENCES customer(id),
                              balance         NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
                              currency        CHAR(3)       NOT NULL DEFAULT 'CDF' CHECK (currency = 'CDF'),
                              status          VARCHAR(20)   NOT NULL CHECK (status IN ('ACTIVE','BLOCKED','CLOSED')),
                              version         BIGINT        NOT NULL DEFAULT 0,
                              created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_account_customer ON bank_account(customer_id);

CREATE TABLE bank_operation (
                                id               UUID PRIMARY KEY,
                                reference        VARCHAR(64)   NOT NULL UNIQUE,
                                account_id       UUID          NOT NULL REFERENCES bank_account(id),
                                type             VARCHAR(10)   NOT NULL CHECK (type IN ('DEBIT','CREDIT')),
                                amount           NUMERIC(19,2) NOT NULL CHECK (amount > 0),
                                status           VARCHAR(10)   NOT NULL CHECK (status IN ('SUCCESS','REJECTED')),
                                rejection_reason VARCHAR(30),
                                balance_after    NUMERIC(19,2),
                                created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
                                CHECK ( (status = 'SUCCESS'  AND balance_after IS NOT NULL AND rejection_reason IS NULL)
                                    OR (status = 'REJECTED' AND rejection_reason IS NOT NULL) )
);
CREATE INDEX idx_operation_account_date ON bank_operation(account_id, created_at DESC);