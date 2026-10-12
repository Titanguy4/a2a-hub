-- ============================================================================
-- V5__active_agents_vector_hnsw_optimization.sql
-- Optimizes vector search for active agents (eliminating post-filter vacancy)
-- ============================================================================

-- 1. Create partial HNSW index strictly for active agents (HEALTHY or DEGRADED)
-- Excludes OFFLINE agents from the vector graph to eliminate post-filter vacancy
-- and preserve PostgreSQL shared_buffers memory footprint.
CREATE INDEX IF NOT EXISTS idx_agents_active_embedding_hnsw
ON agents USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64)
WHERE status IN ('HEALTHY', 'DEGRADED');

COMMENT ON INDEX idx_agents_active_embedding_hnsw IS 
'Partial HNSW index isolating active agents to eliminate post-filter vector blindness and guarantee available candidates';
