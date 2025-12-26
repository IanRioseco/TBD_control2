<template>
  <main class="tasks-page">
    <h1 class="page-title">Gestión de Tareas</h1>

    <section class="content">
      <!-- Formulario sirve para Crear o Editar -->
      <div class="form-section">
        <h2 v-if="isEditing">Editar Tarea</h2>
        <h2 v-else>Crear Tarea</h2>

        <TaskForm
          :sectors="sectors"
          :initial-data="editingTask"
          :is-edit="isEditing"
          @task-created="handleTaskSaved"
          @cancel-edit="cancelEdit"
        />
      </div>

      <div class="list-section">
        <!-- REQUISITO 3: Filtros -->
        <div class="filters">
          <input v-model="searchQuery" placeholder="Buscar por título..." />
          <select v-model="filterStatus">
            <option value="all">Todas</option>
            <option value="pending">Pendientes</option>
            <option value="finished">Completadas</option>
          </select>
        </div>

        <p v-if="loading">Cargando...</p>
        <p v-if="error" class="error">{{ error }}</p>

        <TaskList
          :tasks="filteredTasks"
          @delete-task="handleDeleteTask"
          @toggle-status="handleToggleStatus"
          @edit-task="handleEditClick"
        />
      </div>
    </section>
  </main>
</template>

<script>
import TaskForm from '../components/TaskForm.vue';
import TaskList from '../components/TaskList.vue';
import taskService from '../services/taskService';
import authService from '../services/authService';

export default {
  name: 'TasksView',
  components: { TaskForm, TaskList },
  data() {
    return {
      tasks: [],
      sectors: [],
      loading: false,
      error: '',

      // Filtros
      searchQuery: '',
      filterStatus: 'all',

      // Edición
      isEditing: false,
      editingTask: null
    };
  },
  computed: {
    // REQUISITO 3: Lógica de filtrado
    filteredTasks() {
      return this.tasks.filter(task => {
        const matchesSearch = task.title.toLowerCase().includes(this.searchQuery.toLowerCase());

        let matchesStatus = true;
        if (this.filterStatus === 'pending') matchesStatus = !task.finished;
        if (this.filterStatus === 'finished') matchesStatus = task.finished;

        return matchesSearch && matchesStatus;
      });
    }
  },
  async created() {
    if (!authService.getCurrentUser()) {
      this.$router.push('/login');
      return;
    }
    await this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const [sectors, tasks] = await Promise.all([
          taskService.getSectors(),
          taskService.getTasks(),
        ]);
        this.sectors = sectors;
        this.tasks = tasks;
      } catch (err) {
        console.error(err);
        this.error = 'Error cargando datos';
      } finally {
        this.loading = false;
      }
    },
    async handleTaskSaved() {
      await this.loadData(); // Recargar lista
      this.cancelEdit();
    },
    async handleDeleteTask(id) {
      if(!confirm("¿Estás seguro?")) return;
      try {
        await taskService.deleteTask(id);
        this.tasks = this.tasks.filter(t => t.id !== id);
      } catch (err) { console.error(err); }
    },
    // REQUISITO 2: Marcar completada
    async handleToggleStatus(task) {
      try {
        // Usar endpoint dedicado para toggle de estado
        const updated = await taskService.toggleFinished(task.id);
        task.finished = updated.finished; // Actualizar UI con respuesta
      } catch (err) {
        console.error(err);
        alert("Error al actualizar estado");
      }
    },
    // REQUISITO 2: Editar
    handleEditClick(task) {
      this.isEditing = true;
      // Clonar objeto para no mutar la lista directamente
      this.editingTask = { ...task };
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    cancelEdit() {
      this.isEditing = false;
      this.editingTask = null;
    }
  }
};
</script>

<style scoped>
.tasks-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  background: #1a1a1a;
  border: 1px solid #2b2b2b;
  border-radius: 12px;
  margin-top: 0;
}
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 30px; color: white; }

.content { display: grid; grid-template-columns: 1fr 1.5fr; gap: 2rem; }
@media (max-width: 768px) { .content { grid-template-columns: 1fr; } }

.filters { display: flex; gap: 10px; margin-bottom: 1rem; }
.filters input { flex: 1; padding: 8px; border-radius: 4px; border: 1px solid #555; background: #333; color: white; }
.filters select { padding: 8px; border-radius: 4px; border: 1px solid #555; background: #333; color: white; }
.error { color: #ff4444; }
</style>