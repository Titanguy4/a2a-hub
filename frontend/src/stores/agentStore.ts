import { defineStore } from 'pinia';
import { api, type Agent } from '../services/api';
import { ref } from 'vue';

export const useAgentStore = defineStore('agents', () => {
  const agents = ref<Agent[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const fetchAgents = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await api.get<Agent[]>('/agents');
      agents.value = response.data;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch agents';
    } finally {
      loading.value = false;
    }
  };

  const registerAgent = async (url: string) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await api.post<Agent>('/agents', { url });
      agents.value.push(response.data);
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to register agent';
      return false;
    } finally {
      loading.value = false;
    }
  };

  const unregisterAgent = async (id: string) => {
    try {
      await api.delete(`/agents/${id}`);
      agents.value = agents.value.filter(a => a.id !== id);
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to unregister agent';
    }
  };

  return {
    agents,
    loading,
    error,
    fetchAgents,
    registerAgent,
    unregisterAgent
  };
});
