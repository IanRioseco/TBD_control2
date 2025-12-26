<template>
  <div class="map-view">
    <div class="map-header">
      <h1>📍 Mapa de Tareas</h1>
      <p>Visualiza tus tareas por ubicación geoespacial</p>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Cargando mapa...</p>
    </div>

    <div v-else class="map-wrapper">
      <TaskMap :tasks="tasks" :sectors="sectors" :userLocation="userLocation" />
    </div>

    <div class="legend">
      <div class="legend-item">
        <span class="legend-color" style="background-color: #4CAF50;"></span>
        <span>Tu ubicación</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #2196F3;"></span>
        <span>Sectores</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #FFD700;"></span>
        <span>Tarea pendiente</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #FF6B6B;"></span>
        <span>Tarea importante</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #90EE90;"></span>
        <span>Tarea completada</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TaskMap from '../components/TaskMap.vue';
import api from '../services/api';
import taskService from '../services/taskService';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(true);
const tasks = ref([]);
const sectors = ref([]);
const userLocation = ref(null);

const loadData = async () => {
  try {
    const userId = Number(localStorage.getItem('userId'));

    // Cargar tareas del usuario
    const tasksRes = await taskService.getTasks();
    tasks.value = tasksRes;

    // Cargar sectores
    const sectorsRes = await taskService.getSectors();
    sectors.value = sectorsRes;

    // Cargar ubicación del usuario
    const userRes = await api.get(`/api/admin/users/${userId}`);
    if (userRes.data && userRes.data.latitude && userRes.data.longitude) {
      userLocation.value = {
        latitude: userRes.data.latitude,
        longitude: userRes.data.longitude
      };
    }
  } catch (err) {
    console.error('Error cargando datos del mapa', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const token = localStorage.getItem('token');
  if (!token) {
    router.push('/login');
    return;
  }
  loadData();
});
</script>

<style scoped>
.map-view {
  min-height: 100vh;
  background: #1a1a1a;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.map-header {
  text-align: center;
  color: white;
  margin-bottom: 20px;
  padding: 20px;
  background: #2a2a2a;
  border-radius: 8px;
}

.map-header h1 {
  margin: 0;
  font-size: 28px;
  color: #41b883;
}

.map-header p {
  margin: 10px 0 0 0;
  color: #aaa;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  color: #aaa;
}

.spinner {
  border: 4px solid #444;
  border-top: 4px solid #41b883;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.map-wrapper {
  flex: 1;
  background: #2a2a2a;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  min-height: 600px;
}

.legend {
  background: #2a2a2a;
  border-radius: 8px;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #ccc;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #555;
}
</style>
