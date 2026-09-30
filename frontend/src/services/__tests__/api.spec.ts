import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { api, discoveryApi, type Agent, type SkillSummary, type TagSummary } from '../api';

const baseURL = 'http://localhost:8080/api/v1';

const sampleAgent: Agent = {
  id: 'a0000000-0000-0000-0000-000000000001',
  name: 'StockAgent',
  description: 'Financial market ticker tracker',
  url: 'https://stocks.agent.net',
  version: '1.0.0',
  providerName: 'FinCorp',
  status: 'HEALTHY',
  authType: 'API_KEY',
  registeredAt: '2026-09-30T10:00:00Z',
  lastSeenAt: null,
  agentCard: {
    name: 'StockAgent',
    description: 'Financial market ticker tracker',
    url: 'https://stocks.agent.net',
    version: '1.0.0',
    skills: [
      { id: 'stock_quote', name: 'Stock Quote', description: 'Real-time stock price', tags: ['finance', 'stocks'] }
    ],
    capabilities: { realtime: true },
    supportedInterfaces: ['REST']
  }
};

const server = setupServer(
  http.get(`${baseURL}/agents`, () => {
    return HttpResponse.json([sampleAgent]);
  }),
  http.post(`${baseURL}/agents`, async ({ request }) => {
    const body = (await request.json()) as { url: string };
    if (!body.url || !body.url.startsWith('http')) {
      return HttpResponse.json({ error: 'Validation Failed', message: 'Invalid URL' }, { status: 400 });
    }
    return HttpResponse.json(sampleAgent, { status: 201 });
  }),
  http.get(`${baseURL}/discover`, ({ request }) => {
    const url = new URL(request.url);
    const tag = url.searchParams.get('tag');
    if (tag === 'finance') {
      return HttpResponse.json([sampleAgent]);
    }
    return HttpResponse.json([]);
  }),
  http.get(`${baseURL}/skills`, () => {
    const skills: SkillSummary[] = [
      {
        skillId: 'stock_quote',
        name: 'Stock Quote',
        description: 'Real-time stock price',
        tags: ['finance'],
        agentCount: 1,
        agentIds: ['a0000000-0000-0000-0000-000000000001']
      }
    ];
    return HttpResponse.json(skills);
  }),
  http.get(`${baseURL}/tags`, () => {
    const tags: TagSummary[] = [{ tag: 'finance', count: 1 }];
    return HttpResponse.json(tags);
  })
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('API Network Contracts with MSW', () => {
  it('GET /agents returns registered agents list', async () => {
    const response = await api.get<Agent[]>('/agents');
    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(1);
    expect(response.data[0].name).toBe('StockAgent');
  });

  it('POST /agents registers agent and returns 201 Created', async () => {
    const response = await api.post<Agent>('/agents', { url: 'https://stocks.agent.net' });
    expect(response.status).toBe(201);
    expect(response.data.id).toBe(sampleAgent.id);
  });

  it('discoveryApi.discover queries with search params', async () => {
    const response = await discoveryApi.discover({ tag: 'finance' });
    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(1);
    expect(response.data[0].name).toBe('StockAgent');
  });

  it('discoveryApi.getSkills returns aggregated skill summary contracts', async () => {
    const response = await discoveryApi.getSkills();
    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(1);
    expect(response.data[0].skillId).toBe('stock_quote');
    expect(response.data[0].agentCount).toBe(1);
  });

  it('discoveryApi.getTags returns tag counts', async () => {
    const response = await discoveryApi.getTags();
    expect(response.status).toBe(200);
    expect(response.data).toEqual([{ tag: 'finance', count: 1 }]);
  });
});
