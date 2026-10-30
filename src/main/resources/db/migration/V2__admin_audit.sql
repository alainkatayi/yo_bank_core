CREATE TABLE admin_audit_log (
    id             UUID PRIMARY KEY,
    actor_id       VARCHAR(64)  NOT NULL,
    actor_username VARCHAR(100) NOT NULL,
    action         VARCHAR(40)  NOT NULL,
    target_type    VARCHAR(20)  NOT NULL,
    target_id      VARCHAR(64)  NOT NULL,
    details        TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (action IN (
        'CUSTOMER_CREATED',
        'ACCOUNT_OPENED',
        'ACCOUNT_DEPOSIT',
        'ACCOUNT_BLOCKED',
        'ACCOUNT_UNBLOCKED',
        'ACCOUNT_CLOSED',
        'CUSTOMER_SUSPENDED',
        'CUSTOMER_REACTIVATED'
    )),
    CHECK (target_type IN ('CUSTOMER', 'ACCOUNT'))
);

CREATE INDEX idx_admin_audit_created ON admin_audit_log (created_at DESC);
CREATE INDEX idx_admin_audit_actor_created ON admin_audit_log (actor_id, created_at DESC);
