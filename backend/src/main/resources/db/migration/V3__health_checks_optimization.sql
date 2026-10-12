-- ============================================================================
-- V3__health_checks_optimization.sql
-- Optimizes indexing for FIFO pruning, SLA analytics, and enables Hibernate batch inserts
-- ============================================================================

-- 1. Create dedicated Sequence for Hibernate 6 pooled-lo optimizer (allocationSize = 50)
CREATE SEQUENCE IF NOT EXISTS health_checks_seq
    INCREMENT BY 50
    START WITH 100000;

-- Sync sequence with current max ID from existing records
SELECT setval('health_checks_seq', COALESCE((SELECT MAX(id) FROM health_checks), 0) + 100, true);

-- 2. Add Index on checked_at for FIFO Pruning (Resolves Leftmost Prefix Pitfall)
-- Enables O(log N) index range scans for DELETE ... WHERE checked_at < :cutoff
CREATE INDEX IF NOT EXISTS idx_health_checks_checked_at 
ON health_checks (checked_at);

-- 3. Add Partial Covering Index for Latency Aggregates
-- Contains only HEALTHY records and includes latency_ms for zero-heap Index-Only Scan
CREATE INDEX IF NOT EXISTS idx_health_checks_healthy_latency 
ON health_checks (checked_at, latency_ms) 
WHERE status = 'HEALTHY';

-- 4. Tune autovacuum for high-churn table to eliminate MVCC dead-tuple bloat
ALTER TABLE health_checks SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_limit = 1000,
    autovacuum_vacuum_cost_delay = 2
);

COMMENT ON INDEX idx_health_checks_checked_at IS 'Supports keyset batch pruning without seq scanning';
COMMENT ON INDEX idx_health_checks_healthy_latency IS 'Partial covering index for hourly SLA latency analytics';
