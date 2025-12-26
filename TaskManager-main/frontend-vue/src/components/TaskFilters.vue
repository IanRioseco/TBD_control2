<template>
  <div class="filters">
    <div class="group">
      <label>Estado:</label>
      <select v-model="localStatus">
        <option value="all">Todas</option>
        <option value="pending">Pendientes</option>
        <option value="finished">Completadas</option>
        <option value="important">Importantes</option>
      </select>
    </div>

    <div class="group">
      <label>Buscar:</label>
      <input
        v-model="localSearch"
        type="text"
        placeholder="Título o descripción..."
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  status: {
    type: String,
    default: 'all'
  },
  search: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:status', 'update:search']);

const localStatus = ref(props.status);
const localSearch = ref(props.search);

watch(localStatus, (value) => emit('update:status', value));
watch(localSearch, (value) => emit('update:search', value));
</script>

<style scoped>
.filters {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  margin-bottom: 1rem;
}

.group {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
}

input,
select {
  margin-top: 0.25rem;
  padding: 0.4rem 0.6rem;
  border-radius: 4px;
  border: 1px solid #d1d5db;
}
</style>
