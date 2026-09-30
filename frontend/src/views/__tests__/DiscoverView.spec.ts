import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import DiscoverView from '../DiscoverView.vue';
import { discoveryApi, type Agent, type SkillSummary, type TagSummary } from '../../services/api';

const mockAgents: Agent[] = [
  {
    id: 'agent-1',
    name: 'WeatherAgent',
    description: 'Weather forecaster',
    url: 'https://weather.agent.io',
    version: '1.0.0',
    providerName: null,
    status: 'HEALTHY',
    authType: 'NONE',
    registeredAt: '2026-09-30T10:00:00Z',
    lastSeenAt: null,
    agentCard: {
      name: 'WeatherAgent',
      description: 'Weather forecaster',
      url: 'https://weather.agent.io',
      version: '1.0.0',
      skills: [{ id: 'get_weather', name: 'Get Weather', description: 'Weather', tags: ['weather'] }],
      capabilities: {},
      supportedInterfaces: ['REST']
    }
  }
];

const mockSkills: SkillSummary[] = [
  { skillId: 'get_weather', name: 'Get Weather', description: 'Live weather', tags: ['weather'], agentCount: 1, agentIds: ['agent-1'] }
];

const mockTags: TagSummary[] = [
  { tag: 'weather', count: 1 }
];

describe('DiscoverView Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads and renders initial agents, skills, and tags catalog', async () => {
    vi.spyOn(discoveryApi, 'discover').mockResolvedValue({ data: mockAgents } as any);
    vi.spyOn(discoveryApi, 'getSkills').mockResolvedValue({ data: mockSkills } as any);
    vi.spyOn(discoveryApi, 'getTags').mockResolvedValue({ data: mockTags } as any);

    const wrapper = mount(DiscoverView);
    await flushPromises();

    expect(wrapper.text()).toContain('Discover AI Agents');
    expect(wrapper.text()).toContain('WeatherAgent');
    expect(wrapper.text()).toContain('Get Weather');
    expect(wrapper.text()).toContain('#weather');
    expect(wrapper.text()).toContain('1 agent matches criteria');
  });

  it('renders empty state when no matching agents are returned', async () => {
    vi.spyOn(discoveryApi, 'discover').mockResolvedValue({ data: [] } as any);
    vi.spyOn(discoveryApi, 'getSkills').mockResolvedValue({ data: [] } as any);
    vi.spyOn(discoveryApi, 'getTags').mockResolvedValue({ data: [] } as any);

    const wrapper = mount(DiscoverView);
    await flushPromises();

    expect(wrapper.text()).toContain('No matching agents found');
  });

  it('toggles skill filter when skill badge is clicked', async () => {
    const discoverSpy = vi.spyOn(discoveryApi, 'discover').mockResolvedValue({ data: mockAgents } as any);
    vi.spyOn(discoveryApi, 'getSkills').mockResolvedValue({ data: mockSkills } as any);
    vi.spyOn(discoveryApi, 'getTags').mockResolvedValue({ data: mockTags } as any);

    const wrapper = mount(DiscoverView);
    await flushPromises();

    // Click skill badge
    const skillButton = wrapper.find('button[type="button"]');
    await skillButton.trigger('click');
    await flushPromises();

    expect(discoverSpy).toHaveBeenCalledWith(expect.objectContaining({ skill: 'get_weather' }));
  });
});
