// frontend-vue/src/services/authService.js
import api from './api';

const authService = {
  async login(username, password) {
    // CORRECCIÓN: Agregado /api al endpoint
    const response = await api.post('/api/auth/login', { username, password });
    const data = response.data;

    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);

    return data;
  },

  async register(user) {
    // CORRECCIÓN: Agregado /api al endpoint
    const response = await api.post('/api/auth/register', user);
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  },

  getCurrentUser() {
    const token = localStorage.getItem('token');
    if (!token) return null;

    return {
      token,
      userId: Number(localStorage.getItem('userId')),
      username: localStorage.getItem('username'),
      role: localStorage.getItem('role'),
    };
  },
};

export default authService;