<template>
  <div class="task-list">
    <h2>Mis tareas</h2>

    <p v-if="!tasks.length">No hay tareas para mostrar.</p>

    <ul v-else>
      <li
        v-for="task in tasks"
        :key="task.id"
        :class="{ finished: task.finished, important: task.important, 'finished-late': isFinishedLate(task), 'finished-on-time': isFinishedOnTime(task) }"
      >
        <div class="task-header">
          <!-- REQUISITO 2: Marcar como completada -->
          <input
            type="checkbox"
            :checked="task.finished"
            @change="$emit('toggle-status', task)"
            class="status-checkbox"
          />
          <h3 :class="{ done: task.finished }">{{ task.title }}</h3>
        </div>

        <p class="desc">{{ task.description }}</p>

        <div class="meta">
          <!-- REQUISITO 4: Notificación visual de vencimiento -->
          <span v-if="isOverdue(task)" class="overdue">
            ⚠️ Vencida sin cumplir - Vence: {{ formatDate(task.dueDate) }}
          </span>
          <span v-else :class="{ 'urgent': isUrgent(task.dueDate) && !task.finished }">
            Vence: {{ formatDate(task.dueDate) }}
            <span v-if="isUrgent(task.dueDate) && !task.finished"> (¡Pronto!)</span>
          </span>
          <span v-if="task.sectorName"> | Sector: {{ task.sectorName }}</span>
        </div>

        <div class="task-actions">
          <!-- REQUISITO 2: Editar Tarea -->
          <button class="btn-edit" @click="$emit('edit-task', task)">Editar</button>
          <button class="btn-delete" @click="$emit('delete-task', task.id)">Eliminar</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'TaskList',
  props: {
    tasks: { type: Array, default: () => [] }
  },
  emits: ['delete-task', 'toggle-status', 'edit-task'],
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return '';
      // Ajuste para zona horaria local
      const date = new Date(dateStr);
      return date.toLocaleDateString('es-CL', { timeZone: 'UTC' });
    },
    // Detecta si la tarea está vencida y no completada
    // Una tarea está vencida cuando hoy es DESPUÉS del día de vencimiento
    isOverdue(task) {
      if (!task.dueDate || task.finished) return false;
      
      // Parsear la fecha como YYYY-MM-DD sin hora para evitar problemas de timezone
      const [year, month, day] = task.dueDate.split('-').map(Number);
      const dueDate = new Date(year, month - 1, day); // Mes es 0-indexado
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      dueDate.setHours(0, 0, 0, 0);
      
      // Retorna true solo si today es DESPUÉS de dueDate
      return today > dueDate;
    },
    // Detecta si la tarea se completó a tiempo
    isFinishedOnTime(task) {
      if (!task.finished || !task.dueDate) return false;
      
      const [year, month, day] = task.dueDate.split('-').map(Number);
      const dueDate = new Date(year, month - 1, day);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      dueDate.setHours(0, 0, 0, 0);
      
      // Completada a tiempo si hoy es igual o antes que la fecha de vencimiento
      return today <= dueDate;
    },
    // Detecta si la tarea se completó vencida
    isFinishedLate(task) {
      if (!task.finished || !task.dueDate) return false;
      
      const [year, month, day] = task.dueDate.split('-').map(Number);
      const dueDate = new Date(year, month - 1, day);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      dueDate.setHours(0, 0, 0, 0);
      
      // Completada vencida si hoy es después que la fecha de vencimiento
      return today > dueDate;
    },
    // Lógica de Notificación
    isUrgent(dateStr) {
      if (!dateStr) return false;
      const today = new Date();
      const due = new Date(dateStr);
      const diffTime = due - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      // Si vence en los próximos 3 días o ya venció
      return diffDays <= 3;
    }
  },
};
</script>

<style scoped>
.task-list ul { list-style: none; padding: 0; }
.task-list li {
  background: #2a2a2a;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  border-left: 5px solid #646cff;
}
.task-list li.finished { opacity: 0.7; }
.task-list li.important { border-left-color: #ff4444; }
.task-list li.finished-on-time { border-left-color: #41b883 !important; }
.task-list li.finished-late { border-left-color: #ff4444 !important; }

.task-header { display: flex; align-items: center; gap: 10px; }
.status-checkbox { transform: scale(1.5); cursor: pointer; }
.done { text-decoration: line-through; color: #888; }

.desc { color: #ddd; margin: 8px 0; }
.meta { font-size: 0.9rem; color: #aaa; margin-bottom: 10px; }
.urgent { color: #ff4444; font-weight: bold; }
.overdue { color: #ff4444; font-weight: bold; background: rgba(255, 68, 68, 0.2); padding: 2px 6px; border-radius: 3px; }

.task-actions { display: flex; gap: 10px; }
button { padding: 5px 10px; font-size: 0.9rem; }
.btn-delete { background-color: #b91c1c; color: white; }
.btn-edit { background-color: #d97706; color: white; }
</style>