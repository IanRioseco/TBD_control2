import api from './api';

const statsService = {
  async getUserStats() {
    const userId = localStorage.getItem('userId');
    if (!userId) return null;

    try {
      const [
        tasksPerSector,
        nearestPending,
        topSector2km,
        topSector5km,
        avgDistance
      ] = await Promise.all([
        api.get(`/api/stats/user/${userId}/tasks-per-sector`),
        api.get(`/api/stats/user/${userId}/nearest-pending`),
        api.get(`/api/stats/user/${userId}/top-sector-2km`),
        api.get(`/api/stats/user/${userId}/top-sector-5km`),
        api.get(`/api/stats/user/${userId}/avg-distance-completed`)
      ]);

      return {
        tasksPerSector: tasksPerSector.data,
        nearestPending: nearestPending.data, // Puede venir vacío ("") si no hay tarea
        topSector2km: topSector2km.data,
        topSector5km: topSector5km.data,
        avgDistance: avgDistance.data
      };
    } catch (e) {
      console.error("Error obteniendo stats de usuario", e);
      return null;
    }
  },

  async getGlobalStats() {
    try {
      const [pendingBySector, completedByUserSector] = await Promise.all([
        api.get('/api/stats/pending-by-sector'),
        api.get('/api/stats/completed-by-user-and-sector')
      ]);

      return {
        pendingBySector: pendingBySector.data,
        completedByUserSector: completedByUserSector.data
      };
    } catch (e) {
      console.error("Error obteniendo stats globales", e);
      return { pendingBySector: [], completedByUserSector: [] };
    }
  }
};

export default statsService;