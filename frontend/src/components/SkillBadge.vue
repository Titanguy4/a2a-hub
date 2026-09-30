<script setup lang="ts">
defineProps<{
  label: string;
  count?: number;
  active?: boolean;
}>();

defineEmits<{
  (e: 'click'): void;
}>();
</script>

<template>
  <button
    type="button"
    @click="$emit('click')"
    :class="[
      'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all cursor-pointer border',
      active
        ? 'bg-blue-600 text-white border-blue-600 shadow-sm'
        : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
    ]"
  >
    <span>{{ label }}</span>
    <span
      v-if="count !== undefined"
      :class="[
        'px-1.5 py-0.2 rounded-full text-[10px] font-semibold',
        active ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600'
      ]"
    >
      {{ count }}
    </span>
  </button>
</template>
