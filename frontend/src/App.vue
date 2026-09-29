<script setup lang="ts">
import { Network } from 'lucide-vue-next';
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-900">
    <nav class="bg-white border-b border-gray-200 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center gap-2">
            <div class="bg-blue-600 text-white p-1.5 rounded-lg">
              <Network class="w-5 h-5" />
            </div>
            <span class="font-bold text-xl tracking-tight text-gray-900">A2A Hub</span>
          </div>
          <div class="flex items-center space-x-4">
            <router-link to="/" class="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors">Registry</router-link>
            <a href="#" class="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-not-allowed opacity-50">Discovery</a>
            <a href="#" class="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-not-allowed opacity-50">Orchestrate</a>
          </div>
        </div>
      </div>
    </nav>

    <main>
      <router-view />
    </main>
  </div>
</template>
