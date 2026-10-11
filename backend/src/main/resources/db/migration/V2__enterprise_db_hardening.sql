-- V2__enterprise_db_hardening.sql
-- 1. Create RFC 9562 UUIDv7 generator for PostgreSQL 16 using pure SQL (zero external C dependencies)
CREATE OR REPLACE FUNCTION uuid_generate_v7()
RETURNS uuid
LANGUAGE sql
VOLATILE
AS $$
  SELECT encode(
    set_bit(
      set_bit(
        overlay(
          uuid_send(gen_random_uuid())
          PLACING substring(int8send(floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint) FROM 3)
          FROM 1 FOR 6
        ),
        52, 1
      ),
      53, 1
    ),
    'hex'
  )::uuid;
$$;

COMMENT ON FUNCTION uuid_generate_v7() IS 'RFC 9562 UUIDv7 generator for PostgreSQL 16 using clock_timestamp and pgcrypto/gen_random_uuid entropy';

-- 2. Alter tables to use UUIDv7 defaults on the database side for direct SQL operations
ALTER TABLE agents ALTER COLUMN id SET DEFAULT uuid_generate_v7();
ALTER TABLE agent_skills ALTER COLUMN id SET DEFAULT uuid_generate_v7();
ALTER TABLE tasks ALTER COLUMN id SET DEFAULT uuid_generate_v7();

-- 3. Add optimistic locking counters with safe metadata-only defaults (PostgreSQL 11+)
ALTER TABLE agents ADD COLUMN IF NOT EXISTS lock_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS lock_version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN agents.lock_version IS 'Hibernate optimistic locking counter';
COMMENT ON COLUMN tasks.lock_version IS 'Hibernate optimistic locking counter';

-- 4. Add updated_at to agents for audit consistency
ALTER TABLE agents ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp();

-- 5. Create audit trigger to update timestamp only when records actually mutate (prevents false audit writes on no-ops)
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW IS DISTINCT FROM OLD THEN
        NEW.updated_at = clock_timestamp();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_agents_updated_at ON agents;
CREATE TRIGGER trg_agents_updated_at
    BEFORE UPDATE ON agents
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_tasks_updated_at ON tasks;
CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

-- 6. Add explicit indexes on Foreign Keys to eliminate table-lock cascade penalties
CREATE INDEX IF NOT EXISTS idx_agent_skills_agent_id ON agent_skills(agent_id);
CREATE INDEX IF NOT EXISTS idx_health_checks_agent_id ON health_checks(agent_id);
CREATE INDEX IF NOT EXISTS idx_tasks_agent_id ON tasks(agent_id);
