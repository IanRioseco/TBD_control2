<template>
  <div class="task-form-card">
    <form @submit.prevent="onSubmit">
      <div class="form-group">
        <label>Título *</label>
        <input v-model="task.title" required placeholder="Ej: Reparar calle..." />
      </div>

      <div class="form-group">
        <label>Descripción *</label>
        <textarea v-model="task.description" required rows="3"></textarea>
      </div>

      <div class="form-group">
        <label>Vencimiento *</label>
        <input type="date" v-model="task.dueDate" required />
      </div>

      <div class="form-group">
        <label>Sector *</label>
        <select v-model="task.sectorId" required>
          <option value="" disabled>Seleccionar...</option>
          <option v-for="s in sectors" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="form-group checkbox">
        <label>
          <input type="checkbox" v-model="task.important" />
          Importante
        </label>
      </div>

      <div class="actions">
        <button type="submit" class="btn-save">
          {{ isEdit ? 'Actualizar' : 'Crear' }}
        </button>
        <button v-if="isEdit" type="button" @click="$emit('cancel-edit')" class="btn-cancel">
          Cancelar
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import taskService from '../services/taskService';

export default {
  name: 'TaskForm',
  props: {
    sectors: { type: Array, default: () => [] },
    initialData: { type: Object, default: null },
    isEdit: { type: Boolean, default: false }
  },
  data() {
    return {
      task: {
        title: '', description: '', dueDate: '', sectorId: '', important: false
      }
    };
  },
  watch: {
    // Si cambia initialData (cuando le damos a editar), actualizamos el form
    initialData: {
      handler(newData) {
        if (newData) {
          this.task = { ...newData };
          // Asegurar que sectorId sea un número si el select lo requiere
          if(this.task.sector) this.task.sectorId = this.task.sector.id;
        } else {
          this.resetForm();
        }
      },
      immediate: true
    }
  },
  methods: {
    resetForm() {
      this.task = { title: '', description: '', dueDate: '', sectorId: '', important: false };
    },
    async onSubmit() {
      try {
        if (this.isEdit) {
          await taskService.updateTask(this.task.id, this.task);
        } else {
          await taskService.createTask(this.task);
        }
        this.$emit('task-created'); // Notifica al padre para recargar
        this.resetForm();
      } catch (err) {
        console.error(err);
        alert("Error al guardar");
      }
    }
  }
};
</script>

<style scoped>
.task-form-card { background: #2a2a2a; padding: 1.5rem; border-radius: 12px; }
.form-group { margin-bottom: 1rem; text-align: left; }
.form-group label { display: block; margin-bottom: 0.3rem; font-size: 0.9rem; color: #ccc; }
input, textarea, select { width: 100%; padding: 8px; background: #333; border: 1px solid #555; color: white; border-radius: 4px; box-sizing: border-box; }
.checkbox label { display: flex; align-items: center; gap: 8px; cursor: pointer; }
input[type="checkbox"] { width: auto; }
.actions { display: flex; gap: 10px; margin-top: 1.5rem; }
.btn-save { background: #2563eb; color: white; width: 100%; }
.btn-cancel { background: #555; color: white; width: 100%; }
</style>