import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TasksView from '../views/TasksView.vue';
import AdminView from '../views/AdminView.vue';
import QuestionsView from '../views/QuestionsView.vue';
import MapView from '../views/MapView.vue';

const routes = [
  { path: '/', redirect: '/tasks' },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  {
    path: '/tasks',
    name: 'tasks',
    component: TasksView,
    meta: { requiresAuth: true }
  },
  {
    path: '/preguntas',
    name: 'questions',
    component: QuestionsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/mapa',
    name: 'map',
    component: MapView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminView,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// ... (el resto del beforeEach queda igual) ...
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (to.meta.requiresAuth && !token) {
    next({ name: 'login' });
    return;
  }

  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next({ name: 'tasks' });
    return;
  }

  next();
});

export default router;