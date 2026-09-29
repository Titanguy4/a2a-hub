-- Habilitar extensión para búsqueda vectorial y semántica
CREATE EXTENSION IF NOT EXISTS vector;

-- Agentes registrados
CREATE TABLE agents (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    url            VARCHAR(512) NOT NULL UNIQUE,
    version        VARCHAR(50),
    provider_name  VARCHAR(255),
    status         VARCHAR(20) DEFAULT 'UNKNOWN' 
                   CHECK (status IN ('HEALTHY', 'DEGRADED', 'OFFLINE', 'UNKNOWN')),
    auth_type      VARCHAR(50) DEFAULT 'NONE',   -- NONE, BEARER, API_KEY
    auth_token_enc TEXT,                         -- Token cifrado si aplica
    agent_card     JSONB NOT NULL,
    embedding      vector(768),                  -- Vector embedding
    registered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at   TIMESTAMPTZ,
    CONSTRAINT chk_url_protocol CHECK (url ~* '^https?://')
);

CREATE INDEX idx_agents_status ON agents(status);
CREATE INDEX idx_agents_agent_card_gin ON agents USING gin(agent_card);
CREATE INDEX idx_agents_embedding ON agents USING hnsw (embedding vector_cosine_ops);

-- Skills normalizados para queries estructurados
CREATE TABLE agent_skills (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id    UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    skill_id    VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    tags        TEXT[],
    embedding   vector(768),
    CONSTRAINT uq_agent_skill UNIQUE (agent_id, skill_id)
);

CREATE INDEX idx_agent_skills_tags ON agent_skills USING gin(tags);

-- Health checks con log
CREATE TABLE health_checks (
    id         BIGSERIAL PRIMARY KEY,
    agent_id   UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status     VARCHAR(20) NOT NULL,
    latency_ms INT,
    error_msg  TEXT
);

CREATE INDEX idx_health_checks_agent_time ON health_checks(agent_id, checked_at DESC);

-- Tareas
CREATE TABLE tasks (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id     UUID NOT NULL REFERENCES agents(id) ON DELETE RESTRICT,
    context_id   VARCHAR(255),
    state        VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
                 CHECK (state IN ('SUBMITTED', 'WORKING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    request      JSONB NOT NULL,
    response     JSONB,
    error_detail TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_agent_state ON tasks(agent_id, state);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
