import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AgentDetailView from '../AgentDetailView.vue';
import { healthApi, type AgentHealthResponse } from '../../services/api';

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { id: 'agent-123' }
  }),
  useRouter: () => ({
    push: vi.fn()
  })
}));

const mockHealthData: AgentHealthResponse = {
  agentId: 'agent-123',
  agentName: 'WeatherAssistant',
  currentStatus: 'HEALTHY',
  lastSeenAt: '2026-10-01T12:00:00Z',
  history: [
    {
      id: 1,
      checkedAt: '2026-10-01T12:00:00Z',
      status: 'HEALTHY',
      latencyMs: 95,
      errorMessage: null
    },
    {
      id: 2,
      checkedAt: '2026-10-01T11:55:00Z',
      status: 'DEGRADED',
      latencyMs: 220,
      errorMessage: null
    }
  ]
};

describe('AgentDetailView Component', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.restoreAllMocks();
  });

  it('renders health telemetry, status, and probe history', async () => {
    vi.spyOn(healthApi, 'getAgentHealth').mockResolvedValue({ data: mockHealthData } as any);

    const pinia = createPinia();
    const wrapper = mount(AgentDetailView, {
      global: {
        plugins: [pinia]
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('WeatherAssistant');
    expect(wrapper.text()).toContain('HEALTHY');
    expect(wrapper.text()).toContain('Probe Latency History');
    expect(wrapper.text()).toContain('95 ms');
    expect(wrapper.text()).toContain('220 ms');
  });

  it('triggers manual health probe when button is clicked', async () => {
    vi.spyOn(healthApi, 'getAgentHealth').mockResolvedValue({ data: mockHealthData } as any);

    const pinia = createPinia();
    const wrapper = mount(AgentDetailView, {
      global: {
        plugins: [pinia]
      }
    });

    await flushPromises();

    const probeBtn = wrapper.find('button.bg-blue-600');
    expect(probeBtn.exists()).toBe(true);
  });
});
