import { createRouter, createWebHistory } from 'vue-router';
import AgentRegistryView from '../views/AgentRegistryView.vue';
import DiscoverView from '../views/DiscoverView.vue';

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
    }
  ]
});

export default router;
