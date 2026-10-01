import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAgentStore } from '../agentStore';
import { api, healthApi, type Agent } from '../../services/api';

const mockAgent: Agent = {
  id: 'agent-123',
  name: 'Test Agent',
  description: 'Test Description',
  url: 'https://test.agent.io',
  version: '1.0.0',
  providerName: null,
  status: 'HEALTHY',
  authType: 'NONE',
  registeredAt: '2026-09-30T12:00:00Z',
  lastSeenAt: null,
  agentCard: {
    name: 'Test Agent',
    description: 'Test Description',
    url: 'https://test.agent.io',
    version: '1.0.0',
    skills: [],
    capabilities: {},
    supportedInterfaces: ['REST']
  }
};

describe('useAgentStore Pinia Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
    vi.spyOn(healthApi, 'getStats').mockResolvedValue({
      data: {
        totalAgents: 1,
        healthyAgents: 1,
        degradedAgents: 0,
        offlineAgents: 0,
        unknownAgents: 0,
        averageLatencyMs: 25.0
      }
    } as any);
  });

  it('initializes with default empty state', () => {
    const store = useAgentStore();
    expect(store.agents).toEqual([]);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('fetchAgents populates agents list on success', async () => {
    const store = useAgentStore();
    vi.spyOn(api, 'get').mockResolvedValueOnce({ data: [mockAgent] });

    await store.fetchAgents();

    expect(store.agents).toHaveLength(1);
    expect(store.agents[0].id).toBe('agent-123');
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('fetchAgents sets error message on API failure', async () => {
    const store = useAgentStore();
    vi.spyOn(api, 'get').mockRejectedValueOnce({
      response: { data: { message: 'Network error connecting to registry' } }
    });

    await store.fetchAgents();

    expect(store.agents).toEqual([]);
    expect(store.error).toBe('Network error connecting to registry');
    expect(store.loading).toBe(false);
  });

  it('registerAgent appends agent to list on success', async () => {
    const store = useAgentStore();
    vi.spyOn(api, 'post').mockResolvedValueOnce({ data: mockAgent });

    const success = await store.registerAgent('https://test.agent.io');

    expect(success).toBe(true);
    expect(store.agents).toHaveLength(1);
    expect(store.agents[0].name).toBe('Test Agent');
  });

  it('unregisterAgent removes agent by ID', async () => {
    const store = useAgentStore();
    store.agents = [mockAgent];
    vi.spyOn(api, 'delete').mockResolvedValueOnce({});

    await store.unregisterAgent('agent-123');

    expect(store.agents).toHaveLength(0);
  });
});
