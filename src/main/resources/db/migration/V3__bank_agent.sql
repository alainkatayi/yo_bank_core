CREATE TABLE bank_agent (
    id             UUID PRIMARY KEY,
    username       VARCHAR(100) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150),
    role           VARCHAR(20)  NOT NULL,
    enabled        BOOLEAN      NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT bank_agent_username_key UNIQUE (username),
    CONSTRAINT bank_agent_role_check CHECK (role IN ('BANK_ADMIN', 'BANK_AUDITOR'))
);

CREATE INDEX idx_bank_agent_username ON bank_agent (username);

DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'admin_audit_log'
          AND con.contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE admin_audit_log DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE admin_audit_log ADD CONSTRAINT admin_audit_log_action_check CHECK (action IN (
    'CUSTOMER_CREATED',
    'ACCOUNT_OPENED',
    'ACCOUNT_DEPOSIT',
    'ACCOUNT_BLOCKED',
    'ACCOUNT_UNBLOCKED',
    'ACCOUNT_CLOSED',
    'CUSTOMER_SUSPENDED',
    'CUSTOMER_REACTIVATED',
    'AGENT_CREATED'
));

ALTER TABLE admin_audit_log ADD CONSTRAINT admin_audit_log_target_type_check
    CHECK (target_type IN ('CUSTOMER', 'ACCOUNT', 'AGENT'));

INSERT INTO bank_agent (id, username, password_hash, first_name, last_name, email, role, enabled)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'admin1',
        '$2a$10$S5XOUzoRu0UXSoA6CNxJiu5KckVKT/3yHhKfKqPDe7AtgQeqplxum',
        'Amina',
        'Admin',
        'admin1@bank.local',
        'BANK_ADMIN',
        TRUE
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'auditor1',
        '$2a$10$0Nv4C9w2cpI.QwVtSPNrseD4FLJonw982aCGIlzXbHPE2ML6Mh0vm',
        'Bruno',
        'Auditeur',
        'auditor1@bank.local',
        'BANK_AUDITOR',
        TRUE
    );
