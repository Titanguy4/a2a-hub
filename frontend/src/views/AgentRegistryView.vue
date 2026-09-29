<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useAgentStore } from '../stores/agentStore';
import AgentCardComponent from '../components/AgentCard.vue';
import { Search, Plus } from 'lucide-vue-next';

const store = useAgentStore();
const showRegisterModal = ref(false);
const newAgentUrl = ref('');

onMounted(() => {
  store.fetchAgents();
});

const handleRegister = async () => {
  if (!newAgentUrl.value) return;
  const success = await store.registerAgent(newAgentUrl.value);
  if (success) {
    showRegisterModal.value = false;
    newAgentUrl.value = '';
  }
};
</script>

<template>
  <div class="max-w-7xl mx-auto p-6">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 tracking-tight">Agent Registry</h1>
        <p class="text-gray-500 mt-1">Manage and discover connected A2A intelligent agents.</p>
      </div>
      <button 
        @click="showRegisterModal = true"
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors shadow-sm"
      >
        <Plus class="w-5 h-5" />
        Register Agent
      </button>
    </div>

    <!-- Error Banner -->
    <div v-if="store.error" class="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm text-red-700">{{ store.error }}</p>
        </div>
      </div>
    </div>

    <!-- Agent Grid -->
    <div v-if="store.loading && store.agents.length === 0" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>
    
    <div v-else-if="store.agents.length === 0" class="text-center py-20 border-2 border-dashed border-gray-200 rounded-xl">
      <Search class="mx-auto h-12 w-12 text-gray-400 mb-4" />
      <h3 class="text-lg font-medium text-gray-900 mb-1">No agents registered</h3>
      <p class="text-gray-500">Get started by registering a new A2A agent.</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <AgentCardComponent 
        v-for="agent in store.agents" 
        :key="agent.id" 
        :agent="agent"
        @unregister="store.unregisterAgent(agent.id)"
      />
    </div>

    <!-- Register Modal -->
    <div v-if="showRegisterModal" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-lg font-semibold text-gray-900">Register New Agent</h3>
        </div>
        <div class="p-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Agent URL</label>
          <input 
            v-model="newAgentUrl"
            type="url" 
            placeholder="https://api.example.com/agent"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-shadow"
            @keyup.enter="handleRegister"
          />
          <p class="text-xs text-gray-500 mt-2">
            The hub will fetch the agent's capabilities from <code class="bg-gray-100 px-1 py-0.5 rounded">/.well-known/agent-card.json</code>
          </p>
        </div>
        <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3">
          <button 
            @click="showRegisterModal = false"
            class="px-4 py-2 text-gray-700 hover:bg-gray-200 rounded-lg font-medium transition-colors"
          >
            Cancel
          </button>
          <button 
            @click="handleRegister"
            :disabled="store.loading || !newAgentUrl"
            class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2"
          >
            <span v-if="store.loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
            Register
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
