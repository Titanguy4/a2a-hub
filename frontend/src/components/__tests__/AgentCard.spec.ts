import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AgentCard from '../AgentCard.vue';
import type { Agent } from '../../services/api';

const mockAgent: Agent = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  name: 'WeatherAgent',
  description: 'Provides real-time meteorological forecasts and climate metrics.',
  url: 'https://weather.agent.internal',
  version: '1.2.0',
  providerName: 'OpenWeather',
  status: 'HEALTHY',
  authType: 'BEARER',
  registeredAt: '2026-09-28T10:00:00Z',
  lastSeenAt: null,
  agentCard: {
    name: 'WeatherAgent',
    description: 'Provides real-time meteorological forecasts and climate metrics.',
    url: 'https://weather.agent.internal',
    version: '1.2.0',
    skills: [
      { id: 'get_weather', name: 'Get Weather', description: 'Fetches weather', tags: ['weather'] },
      { id: 'forecast_7d', name: '7-Day Forecast', description: '7-day weather forecast', tags: ['forecast'] },
      { id: 'air_quality', name: 'Air Quality', description: 'Air quality index', tags: ['environment'] },
      { id: 'storm_radar', name: 'Storm Radar', description: 'Real-time storm radar', tags: ['radar'] }
    ],
    capabilities: { streaming: true },
    supportedInterfaces: ['JSON-RPC', 'REST']
  }
};

describe('AgentCard Component', () => {
  it('renders agent name, url, description, version, and status', () => {
    const wrapper = mount(AgentCard, {
      props: { agent: mockAgent }
    });

    expect(wrapper.text()).toContain('WeatherAgent');
    expect(wrapper.text()).toContain('https://weather.agent.internal');
    expect(wrapper.text()).toContain('Provides real-time meteorological forecasts');
    expect(wrapper.text()).toContain('v1.2.0');
    expect(wrapper.text()).toContain('BEARER');
    expect(wrapper.text()).toContain('HEALTHY');
  });

  it('renders up to 3 skills and +1 more indicator when 4 skills exist', () => {
    const wrapper = mount(AgentCard, {
      props: { agent: mockAgent }
    });

    expect(wrapper.text()).toContain('Get Weather');
    expect(wrapper.text()).toContain('7-Day Forecast');
    expect(wrapper.text()).toContain('Air Quality');
    expect(wrapper.text()).toContain('+1 more');
  });

  it('emits unregister event when delete button is clicked', async () => {
    const wrapper = mount(AgentCard, {
      props: { agent: mockAgent }
    });

    const unregisterBtn = wrapper.find('button[title="Unregister Agent"]');
    expect(unregisterBtn.exists()).toBe(true);

    await unregisterBtn.trigger('click');
    expect(wrapper.emitted('unregister')).toHaveLength(1);
  });
});
