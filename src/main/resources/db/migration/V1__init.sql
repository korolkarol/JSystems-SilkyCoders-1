CREATE TABLE sessions (
    id                       TEXT PRIMARY KEY,
    request_type             TEXT NOT NULL
                             CHECK (request_type IN ('REKLAMACJA', 'ZWROT')),
    product_name             TEXT NOT NULL,
    purchase_date            TEXT NOT NULL,
    description              TEXT NOT NULL,
    decision_outcome         TEXT
                             CHECK (decision_outcome IN ('ACCEPT', 'REJECT')),
    decision_explanation     TEXT,
    mismatch_recommendation  TEXT,
    created_at               TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE chat_messages (
    id          TEXT PRIMARY KEY,
    session_id  TEXT NOT NULL
                REFERENCES sessions(id) ON DELETE CASCADE,
    role        TEXT NOT NULL CHECK (role IN ('USER', 'ASSISTANT')),
    content     TEXT NOT NULL,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX idx_chat_messages_session ON chat_messages (session_id, created_at);
