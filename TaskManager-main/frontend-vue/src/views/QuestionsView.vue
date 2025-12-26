<template>
  <div class="questions-page">
    <header class="header-card">
      <h1>📊 Respuestas al Control 2</h1>
      <p>
        Usuario consultado: <span class="highlight">{{ username }}</span>
      </p>
    </header>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Calculando métricas espaciales...</p>
    </div>

    <div v-else class="grid-layout">

      <!-- COLUMNA IZQUIERDA: Estadísticas del Usuario -->
      <div class="col">
        <h2 class="section-title">👤 Métricas Personales</h2>

        <div class="card">
          <div class="q-number">1</div>
          <h3>Tareas realizadas por sector</h3>
          <div v-if="userStats.tasksPerSector.length" class="list-content">
            <ul>
              <li v-for="s in userStats.tasksPerSector" :key="s.sectorId">
                Sector <b>{{ s.sectorName }}</b>: {{ s.total }} tarea(s).
              </li>
            </ul>
          </div>
          <p v-else class="no-data">Sin tareas completadas.</p>
        </div>

        <div class="card">
          <div class="q-number">2 & 6</div>
          <h3>Tarea pendiente más cercana</h3>
          <div v-if="userStats.nearestPending && userStats.nearestPending.id" class="data-box">
            <p><strong>{{ userStats.nearestPending.title }}</strong></p>
            <p class="small">📍 {{ userStats.nearestPending.sectorName }}</p>
            <p class="small">📅 Vence: {{ formatDate(userStats.nearestPending.dueDate) }}</p>
          </div>
          <p v-else class="no-data">No hay pendientes cercanas.</p>
        </div>

        <div class="card">
          <div class="q-number">3</div>
          <h3>Sector top en radio de 2 km</h3>
          <div v-if="userStats.topSector2km" class="result-big">
            {{ userStats.topSector2km.sectorName }}
            <span>({{ userStats.topSector2km.total }} completadas)</span>
          </div>
          <p v-else class="no-data">Ninguno encontrado en 2km.</p>
        </div>

        <div class="card">
          <div class="q-number">8</div>
          <h3>Sector top en radio de 5 km</h3>
          <div v-if="userStats.topSector5km" class="result-big green">
            {{ userStats.topSector5km.sectorName }}
            <span>({{ userStats.topSector5km.total }} completadas)</span>
          </div>
          <p v-else class="no-data">Ninguno encontrado en 5km.</p>
        </div>

        <div class="card">
          <div class="q-number">4 & 9</div>
          <h3>Promedio distancia (Usuario ↔ Tareas)</h3>
          <div v-if="userStats.avgDistance !== null" class="result-number">
            {{ formatDistance(userStats.avgDistance) }}
          </div>
          <p v-else class="no-data">Faltan datos para calcular.</p>
        </div>
      </div>

      <!-- COLUMNA DERECHA: Estadísticas Globales -->
      <div class="col">
        <h2 class="section-title">🌍 Métricas Globales</h2>

        <div class="card">
          <div class="q-number">5</div>
          <h3>Concentración de tareas pendientes</h3>
          <table v-if="globalStats.pendingBySector.length" class="simple-table">
            <tr v-for="s in globalStats.pendingBySector" :key="s.sectorId">
              <td>{{ s.sectorName }}</td>
              <td class="text-right"><b>{{ s.total }}</b></td>
            </tr>
          </table>
          <p v-else class="no-data">No hay pendientes globales.</p>
        </div>

        <div class="card">
          <div class="q-number">7</div>
          <h3>Tareas completadas por usuario/sector</h3>
          <table v-if="globalStats.completedByUserSector.length" class="full-table">
            <thead>
              <tr>
                <th>Usuario</th>
                <th>Sector</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in globalStats.completedByUserSector" :key="idx">
                <td>{{ row.username }}</td>
                <td>{{ row.sectorName }}</td>
                <td>{{ row.total }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else class="no-data">Nadie ha completado tareas.</p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import statsService from '../services/statsService';
import authService from '../services/authService';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(true);
const username = ref('');

const userStats = ref({
  tasksPerSector: [],
  nearestPending: null,
  topSector2km: null,
  topSector5km: null,
  avgDistance: null
});

const globalStats = ref({
  pendingBySector: [],
  completedByUserSector: []
});

const formatDate = (date) => date ? new Date(date).toLocaleDateString('es-CL') : '-';

const formatDistance = (meters) => {
  if (meters === null || meters === undefined) return '-';
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(2)} km`;
};

onMounted(async () => {
  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    router.push('/login');
    return;
  }
  username.value = currentUser.username;

  try {
    const [uStats, gStats] = await Promise.all([
      statsService.getUserStats(),
      statsService.getGlobalStats()
    ]);

    if (uStats) userStats.value = uStats;
    if (gStats) globalStats.value = gStats;

  } catch (error) {
    console.error("Error:", error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.questions-page { max-width: 1100px; margin: 0 auto; padding: 20px; color: #e0e0e0; font-family: 'Segoe UI', sans-serif; }

.header-card { text-align: center; margin-bottom: 30px; }
.header-card h1 { font-size: 2.2rem; margin-bottom: 5px; color: white; }
.highlight { color: #41b883; font-weight: bold; font-size: 1.1rem; }

.grid-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 768px) { .grid-layout { grid-template-columns: 1fr; } }

.section-title { font-size: 1.2rem; color: #aaa; border-bottom: 2px solid #444; padding-bottom: 5px; margin-bottom: 15px; }

.card {
  background: #2a2a2a;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  position: relative;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
}

.q-number {
  position: absolute; top: 15px; right: 15px;
  background: #444; color: #fff;
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 0.8rem; font-weight: bold;
}

.card h3 { margin-top: 0; font-size: 1rem; color: #646cff; margin-bottom: 15px; padding-right: 30px; }

/* Estilos de datos */
.no-data { color: #666; font-style: italic; }
.list-content ul { padding-left: 20px; margin: 0; }
.list-content li { margin-bottom: 4px; }

.data-box { background: #333; padding: 10px; border-radius: 6px; border-left: 3px solid #eab308; }
.data-box p { margin: 2px 0; }
.small { font-size: 0.9rem; color: #bbb; }

.result-big { font-size: 1.2rem; font-weight: bold; color: white; }
.result-big span { font-size: 0.9rem; font-weight: normal; color: #aaa; margin-left: 5px; }
.result-big.green { color: #41b883; }

.result-number { font-size: 1.8rem; font-weight: bold; color: white; text-align: center; margin: 10px 0; }

/* Tablas */
.simple-table, .full-table { width: 100%; border-collapse: collapse; }
.simple-table td { padding: 5px 0; border-bottom: 1px solid #3d3d3d; }
.full-table th { background: #333; padding: 8px; text-align: left; font-size: 0.9rem; }
.full-table td { padding: 8px; border-bottom: 1px solid #3d3d3d; }
.text-right { text-align: right; }

.loading-container { text-align: center; margin-top: 50px; }
.spinner {
  border: 4px solid rgba(255,255,255,0.1); border-left-color: #646cff;
  border-radius: 50%; width: 40px; height: 40px;
  animation: spin 1s linear infinite; margin: 0 auto 15px;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>