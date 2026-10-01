import { createRouter, createWebHistory } from 'vue-router';
import AgentRegistryView from '../views/AgentRegistryView.vue';
import DiscoverView from '../views/DiscoverView.vue';
import AgentDetailView from '../views/AgentDetailView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'registry',
      component: AgentRegistryView
    },
    {
      path: '/discover',
      name: 'discover',
      component: DiscoverView
    },
    {
      path: '/agents/:id',
      name: 'agent-detail',
      component: AgentDetailView
    }
  ]
});

export default router;
