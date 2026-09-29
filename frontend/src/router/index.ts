import { createRouter, createWebHistory } from 'vue-router';
import AgentRegistryView from '../views/AgentRegistryView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'registry',
      component: AgentRegistryView
    }
  ]
});

export default router;
