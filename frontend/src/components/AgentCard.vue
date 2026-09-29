<script setup lang="ts">
import { computed } from 'vue';
import type { Agent } from '../services/api';
import { Bot, Trash2, Activity, ShieldCheck, Cpu } from 'lucide-vue-next';

const props = defineProps<{
  agent: Agent
}>();

defineEmits<{
  (e: 'unregister'): void
}>();

const statusColor = computed(() => {
  switch (props.agent.status) {
    case 'HEALTHY': return 'bg-emerald-500';
    case 'DEGRADED': return 'bg-yellow-500';
    case 'OFFLINE': return 'bg-red-500';
    default: return 'bg-gray-400';
  }
});

const statusTextClass = computed(() => {
  switch (props.agent.status) {
    case 'HEALTHY': return 'text-emerald-700';
    case 'DEGRADED': return 'text-yellow-700';
    case 'OFFLINE': return 'text-red-700';
    default: return 'text-gray-700';
  }
});

const statusBgClass = computed(() => {
  switch (props.agent.status) {
    case 'HEALTHY': return 'bg-emerald-50';
    case 'DEGRADED': return 'bg-yellow-50';
    case 'OFFLINE': return 'bg-red-50';
    default: return 'bg-gray-50';
  }
});
</script>

<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow group flex flex-col">
    <!-- Header -->
    <div class="p-5 border-b border-gray-100 flex justify-between items-start">
      <div class="flex gap-3">
        <div class="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
          <Bot class="w-6 h-6" />
        </div>
        <div>
          <h3 class="font-semibold text-gray-900 leading-tight">{{ agent.name }}</h3>
          <p class="text-xs text-gray-500 mt-1 font-mono truncate max-w-[200px]" :title="agent.url">
            {{ agent.url }}
          </p>
        </div>
      </div>
      <div class="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="[statusBgClass, statusTextClass]">
        <span class="w-1.5 h-1.5 rounded-full" :class="statusColor"></span>
        {{ agent.status }}
      </div>
    </div>

    <!-- Body -->
    <div class="p-5 flex-grow">
      <p class="text-sm text-gray-600 line-clamp-2 mb-4 h-10">
        {{ agent.description || 'No description provided.' }}
      </p>

      <div class="space-y-3">
        <!-- Version & Auth -->
        <div class="flex gap-4 text-sm text-gray-500">
          <div class="flex items-center gap-1.5">
            <Cpu class="w-4 h-4 text-gray-400" />
            <span>v{{ agent.version || '1.0' }}</span>
          </div>
          <div class="flex items-center gap-1.5">
            <ShieldCheck class="w-4 h-4 text-gray-400" />
            <span>{{ agent.authType }}</span>
          </div>
        </div>
        
        <!-- Skills -->
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">Capabilities ({{ agent.agentCard?.skills?.length || 0 }})</p>
          <div class="flex flex-wrap gap-1.5">
            <span 
              v-for="skill in (agent.agentCard?.skills?.slice(0, 3) || [])" 
              :key="skill.id"
              class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-700"
              :title="skill.description"
            >
              {{ skill.name }}
            </span>
            <span v-if="(agent.agentCard?.skills?.length || 0) > 3" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-500">
              +{{ (agent.agentCard?.skills?.length || 0) - 3 }} more
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="px-5 py-3 bg-gray-50 border-t border-gray-100 flex justify-between items-center opacity-0 group-hover:opacity-100 transition-opacity">
      <div class="flex items-center gap-1.5 text-xs text-gray-500">
        <Activity class="w-3.5 h-3.5" />
        <span>Registered {{ new Date(agent.registeredAt).toLocaleDateString() }}</span>
      </div>
      <button 
        @click="$emit('unregister')"
        class="text-gray-400 hover:text-red-600 transition-colors p-1"
        title="Unregister Agent"
      >
        <Trash2 class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>
