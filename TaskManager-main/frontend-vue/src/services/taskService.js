import api from './api';

const taskService = {
  async getSectors() {
    const response = await api.get('/api/sectors');
    return response.data;
  },

  async getTasks() {
    const userId = Number(localStorage.getItem('userId'));
    const response = await api.get('/api/tasks', {
      params: { userId },
    });
    return response.data;
  },

  async createTask(task, targetUserId = null) {
    const userId = targetUserId || Number(localStorage.getItem('userId'));
    const payload = { ...task, userId };
    const response = await api.post('/api/tasks', payload, { params: { userId } });
    return response.data;
  },

  // MODIFICADO: Ahora respetamos el userId que viene dentro del objeto 'task'
  // o permitimos pasarlo explícitamente.
  async updateTask(id, task) {
    // Si el objeto task ya trae un userId (porque lo seleccionamos en el dropdown del admin), lo usamos.
    // Si no, usamos el del usuario logueado.
    const currentUserId = Number(localStorage.getItem('userId'));
    const payload = {
        ...task,
        userId: task.userId || currentUserId
    };

    const userId = payload.userId;
    const response = await api.put(`/api/tasks/${id}`, payload, { params: { userId } });
    return response.data;
  },

  async deleteTask(id) {
    await api.delete(`/api/tasks/${id}`);
  },

  async toggleFinished(id) {
    const response = await api.patch(`/api/tasks/${id}/toggle`);
    return response.data;
  },
};

export default taskService;