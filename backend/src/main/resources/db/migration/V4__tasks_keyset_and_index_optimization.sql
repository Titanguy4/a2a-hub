-- ============================================================================
-- V4__tasks_keyset_and_index_optimization.sql
-- Optimizes task queries: Keyset index on monotonic UUIDv7, projection covering
-- ============================================================================

-- 1. Composite B-Tree index for O(1) Keyset pagination by agent (UUIDv7 is time-ordered)
CREATE INDEX IF NOT EXISTS idx_tasks_agent_keyset 
ON tasks (agent_id, id DESC);

-- 2. Covering index for task summary projections (Index-Only Scan on PostgreSQL 16)
-- Omits request and response JSONB columns entirely to bypass TOAST tables
CREATE INDEX IF NOT EXISTS idx_tasks_summary_covering 
ON tasks (agent_id, id DESC) 
INCLUDE (context_id, state, created_at, updated_at);

-- 3. Autovacuum tuning for high-churn tasks table to eliminate dead-tuple bloat
ALTER TABLE tasks SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_limit = 1500,
    autovacuum_vacuum_cost_delay = 2
);

COMMENT ON INDEX idx_tasks_agent_keyset IS 'Enables O(1) keyset/cursor seek on monotonic UUIDv7';
COMMENT ON INDEX idx_tasks_summary_covering IS 'Covering index bypassing TOAST table reads for list projections';
