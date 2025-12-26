<template>
  <div class="page">
    <div class="page-card">
      <div class="header-section">
        <h1 class="page-title">Panel de Administración</h1>
        <router-link to="/preguntas" class="btn-questions">
          📊 Ver Respuestas
        </router-link>
      </div>

      <!-- 1. FORMULARIO DE GESTIÓN (CREAR O EDITAR) -->
      <section class="section assign-section" :class="{ 'editing-mode': isEditing }">
        <h2 v-if="isEditing">✏️ Editando y Reasignando Tarea #{{ editingId }}</h2>
        <h2 v-else>➕ Crear y Asignar Nueva Tarea</h2>

        <form @submit.prevent="handleSaveTask" class="assign-form">
          <div class="form-row">
            <!-- SELECCIÓN DE USUARIO (ASIGNACIÓN) -->
            <div class="form-group">
              <label>Asignar a Usuario:</label>
              <select v-model="taskForm.targetUserId" required>
                <option value="" disabled>-- Selecciona un responsable --</option>
                <option v-for="u in users" :key="u.id" :value="u.id">
                  {{ u.username }} ({{ u.role }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Sector Geográfico:</label>
              <select v-model="taskForm.sectorId" required>
                <option value="" disabled>-- Elige sector --</option>
                <option v-for="s in sectors" :key="s.id" :value="s.id">{{ s.name }}</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Título:</label>
              <input v-model="taskForm.title" required placeholder="Ej: Reparación de luminaria..." />
            </div>
            <div class="form-group">
              <label>Vencimiento:</label>
              <input type="date" v-model="taskForm.dueDate" required />
            </div>
          </div>

          <div class="form-group">
            <label>Descripción:</label>
            <textarea v-model="taskForm.description" required rows="2"></textarea>
          </div>

          <div class="form-row bottom-row">
            <div class="form-group checkbox">
              <label>
                <input type="checkbox" v-model="taskForm.important" />
                Marcar como Importante
              </label>
              <label v-if="isEditing" style="margin-left: 15px;">
                <input type="checkbox" v-model="taskForm.finished" />
                Marcar como Finalizada
              </label>
            </div>

            <div class="action-buttons">
              <button v-if="isEditing" type="button" class="btn-cancel" @click="cancelEdit">
                Cancelar Edición
              </button>
              <button type="submit" class="btn-assign" :class="{ 'btn-update': isEditing }" :disabled="processing">
                {{ processing ? 'Guardando...' : (isEditing ? 'Guardar Cambios' : 'Crear Tarea') }}
              </button>
            </div>
          </div>
        </form>
      </section>

      <!-- 2. LISTA DE TAREAS (CON BOTÓN PARA EDITAR) -->
      <section class="section">
        <h2>Base de Datos de Tareas</h2>
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Título</th>
                <th>Sector</th>
                <th>Asignado a</th>
                <th>Estado</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in tasks" :key="t.id" :class="{ 'row-editing': t.id === editingId }">
                <td>{{ t.id }}</td>
                <td>{{ t.title }} <span v-if="t.important">⭐</span></td>
                <td>{{ t.sectorName }}</td>
                <!-- Buscamos el nombre del usuario en la lista de users basado en el ID -->
                <td>{{ getUserName(t.userId) }}</td>
                <td>
                  <span :class="t.finished ? 'status-ok' : 'status-pending'">
                    {{ t.finished ? 'Completada' : 'Pendiente' }}
                  </span>
                </td>
                <td>
                  <button class="btn-edit-row" @click="loadTaskForEdit(t)">
                    Gestionar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 3. RESUMEN Y USUARIOS (Mantenemos esto para referencia visual) -->
      <section class="section">
        <div class="cards-row small">
          <div class="card">
            <p class="card-label">Usuarios Registrados</p>
            <p class="card-value">{{ users.length }}</p>
          </div>
          <div class="card">
            <p class="card-label">Tareas Pendientes Totales</p>
            <p class="card-value">{{ pendingTasks }}</p>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '../services/api';
import taskService from '../services/taskService';
import { useRouter } from 'vue-router';

const router = useRouter();

// Datos
const users = ref([]);
const tasks = ref([]);
const sectors = ref([]);

// Estado del Formulario
const processing = ref(false);
const isEditing = ref(false);
const editingId = ref(null);

const taskForm = ref({
  targetUserId: '',
  title: '',
  description: '',
  dueDate: '',
  sectorId: '',
  important: false,
  finished: false
});

// Computados
const pendingTasks = computed(() => tasks.value.filter((t) => !t.finished).length);

// Helpers
const formatDateForInput = (iso) => {
  if (!iso) return '';
  return new Date(iso).toISOString().split('T')[0];
};

const getUserName = (id) => {
  const u = users.value.find(user => user.id === id);
  return u ? u.username : `ID ${id}`;
};

// Carga Inicial
const loadData = async () => {
  try {
    const [usersRes, tasksRes, sectorsRes] = await Promise.all([
      api.get('/api/admin/users'),
      api.get('/api/admin/tasks'),
      taskService.getSectors()
    ]);
    users.value = usersRes.data;
    tasks.value = tasksRes.data;
    sectors.value = sectorsRes;
  } catch (err) {
    console.error('Error cargando admin data', err);
  }
};

// Acción 1: Cargar tarea en el formulario para editar
const loadTaskForEdit = (task) => {
  isEditing.value = true;
  editingId.value = task.id;

  // Rellenar form
  taskForm.value = {
    targetUserId: task.userId, // Aquí asignamos el usuario actual de la tarea
    title: task.title,
    description: task.description,
    dueDate: formatDateForInput(task.dueDate),
    sectorId: task.sectorId,
    important: task.important,
    finished: task.finished
  };

  // Scroll suave hacia arriba
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// Acción 2: Cancelar edición
const cancelEdit = () => {
  isEditing.value = false;
  editingId.value = null;
  resetForm();
};

const resetForm = () => {
  taskForm.value = {
    targetUserId: '',
    title: '',
    description: '',
    dueDate: '',
    sectorId: '',
    important: false,
    finished: false
  };
};

// Acción 3: Guardar (Crear o Actualizar)
const handleSaveTask = async () => {
  if (!taskForm.value.targetUserId || !taskForm.value.sectorId) {
    alert("Falta seleccionar Usuario o Sector");
    return;
  }

  processing.value = true;

  try {
    if (isEditing.value) {
      // --- MODO EDICIÓN / REASIGNACIÓN ---
      // Preparamos el objeto. Importante: mapeamos targetUserId a userId para el backend
      const payload = {
        ...taskForm.value,
        userId: taskForm.value.targetUserId
      };
      await taskService.updateTask(editingId.value, payload);
      alert("Tarea actualizada y reasignada correctamente");
      cancelEdit(); // Salir modo edición

    } else {
      // --- MODO CREACIÓN ---
      await taskService.createTask(taskForm.value, taskForm.value.targetUserId);
      alert("Tarea creada y asignada correctamente");
      resetForm();
    }

    await loadData(); // Refrescar tabla

  } catch (error) {
    console.error("Error guardando:", error);
    alert("Ocurrió un error al guardar.");
  } finally {
    processing.value = false;
  }
};

onMounted(() => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  if (!token || role !== 'ADMIN') {
    router.push('/login');
    return;
  }
  loadData();
});
</script>

<style scoped>
.page { min-height: 100vh; display: flex; justify-content: center; padding: 32px 16px; background: #1a1a1a; color: #eee; }
.page-card { width: 1100px; max-width: 100%; background: #2a2a2a; border-radius: 12px; padding: 24px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2); }

.header-section { display: flex; justify-content: space-between; align-items: center; gap: 15px; margin-bottom: 20px; }
.page-title { text-align: center; flex: 1; font-size: 24px; font-weight: 700; color: white; }

.btn-questions { display: inline-block; background: #3b82f6; color: white; padding: 10px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; cursor: pointer; transition: background 0.3s ease; white-space: nowrap; }
.btn-questions:hover { background: #2563eb; }

.section { margin-top: 30px; padding-top: 20px; border-top: 1px solid #444; }
.section h2 { font-size: 18px; margin-bottom: 15px; color: #41b883; }

/* Formulario */
.assign-section { background: #252525; padding: 20px; border-radius: 10px; border: 1px solid #444; transition: all 0.3s ease; }
.assign-section.editing-mode { border-color: #d97706; background: #2e251a; } /* Color naranja al editar */

.form-row { display: flex; gap: 20px; flex-wrap: wrap; }
.form-group { flex: 1; display: flex; flex-direction: column; min-width: 200px; margin-bottom: 15px; }
.form-group label { margin-bottom: 5px; font-size: 13px; color: #ccc; }
input, select, textarea { padding: 8px; border-radius: 4px; border: 1px solid #555; background: #333; color: white; }

.checkbox label { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.action-buttons { display: flex; gap: 10px; margin-left: auto; }

.btn-assign { background: #41b883; color: #1a1a1a; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; border: none; }
.btn-assign:hover { background: #34d399; }
.btn-assign.btn-update { background: #d97706; color: white; } /* Botón naranja al editar */
.btn-assign.btn-update:hover { background: #b45309; }

.btn-cancel { background: #555; color: white; padding: 10px 15px; border-radius: 6px; cursor: pointer; border: none; }
.btn-cancel:hover { background: #666; }

/* Tabla */
.table-container { overflow-x: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; background: #333; border-radius: 8px; overflow: hidden; }
.table th { background: #222; padding: 10px; text-align: left; color: #41b883; }
.table td { padding: 10px; border-bottom: 1px solid #444; color: #ddd; }
.row-editing { background-color: rgba(217, 119, 6, 0.15); } /* Resaltar fila editada */

.status-ok { color: #41b883; font-weight: bold; }
.status-pending { color: #fbbf24; font-weight: bold; }

.btn-edit-row { background: #3b82f6; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-edit-row:hover { background: #2563eb; }

/* Cards */
.cards-row { display: flex; gap: 15px; }
.card { background: #333; border-radius: 8px; padding: 15px; flex: 1; text-align: center; }
.card-label { font-size: 12px; color: #aaa; }
.card-value { font-size: 20px; font-weight: bold; }
</style>