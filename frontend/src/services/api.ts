import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// TypeScript interfaces based on Phase 1 models
export interface AgentSkill {
  id: string;
  name: string;
  description: string;
  tags: string[];
}

export interface AgentCard {
  name: string;
  description: string;
  url: string;
  version: string;
  skills: AgentSkill[];
  capabilities: Record<string, any>;
  supportedInterfaces: string[];
}

export interface Agent {
  id: string;
  name: string;
  description: string;
  url: string;
  version: string;
  providerName: string | null;
  status: 'HEALTHY' | 'DEGRADED' | 'OFFLINE' | 'UNKNOWN';
  authType: string;
  agentCard: AgentCard;
  registeredAt: string;
  lastSeenAt: string | null;
}
