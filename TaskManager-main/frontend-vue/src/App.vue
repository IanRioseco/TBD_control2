<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const isAuthenticated = computed(() => !!localStorage.getItem('token'));
const username = computed(() => localStorage.getItem('username') || '');
const role = computed(() => localStorage.getItem('role') || '');
const isAdmin = computed(() => role.value === 'ADMIN');
const isNormalUser = computed(() => !isAdmin.value && isAuthenticated.value);
const isLoginPage = computed(() => route.name === 'login');

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  router.push({ name: 'login' });
};
</script>

<template>
  <div class="app-container">
    <!-- Header Principal - No se muestra en login/register -->
    <header v-if="!isLoginPage" class="app-header-top">
      <h1>Sistema de Gestión de Tareas</h1>
      <div v-if="isAuthenticated" class="user-section">
        <span class="user-label">
          Hola, {{ username }} <span v-if="role">({{ role }})</span>
        </span>
        <button @click="logout" class="logout-btn">Cerrar sesión</button>
      </div>
    </header>

    <!-- Nav secundaria - No se muestra en login/register -->
    <nav v-if="!isLoginPage && isAuthenticated" class="app-nav">
      <router-link v-if="isNormalUser" to="/tasks">Mis tareas</router-link>
      <router-link v-if="isNormalUser" to="/mapa">Mapa</router-link>
      <router-link v-if="isNormalUser" to="/preguntas">Preguntas</router-link>
      <router-link v-if="isAdmin" to="/tasks">Mis tareas</router-link>
      <router-link v-if="isAdmin" to="/mapa">Mapa</router-link>
      <router-link v-if="isAdmin" to="/admin">Admin</router-link>
    </nav>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<!-- Estilos -->
<style>
.app-container { display: flex; flex-direction: column; min-height: 100vh; }

/* Header principal: título + usuario + botón sesión */
.app-header-top {
  background: #1a1a1a;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  border-bottom: 1px solid #444;
  flex-wrap: wrap;
}

.app-header-top h1 {
  margin: 0;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
  white-space: nowrap;
}

.user-label {
  color: #ccc;
  font-weight: bold;
  font-size: 15px;
}

.logout-btn {
  background: #333;
  color: white;
  border: 1px solid #555;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  white-space: nowrap;
}

.logout-btn:hover {
  background: #444;
}

/* Nav secundaria: enlaces de navegación */
.app-nav {
  background: #222;
  padding: 12px 30px;
  display: flex;
  gap: 30px;
  border-bottom: 1px solid #444;
  flex-wrap: wrap;
}

.app-nav a {
  text-decoration: none;
  color: #ccc;
  font-weight: bold;
  font-size: 15px;
  transition: color 0.3s;
  white-space: nowrap;
  padding: 8px 16px;
  border-bottom: 2px solid transparent;
  border-radius: 8px;
}

.app-nav a:hover {
  color: white;
}

.app-nav a.router-link-active {
  color: #41b883;
  border-bottom-color: #41b883;
}

.app-main {
  flex: 1;
  background: #1a1a1a;
}

@media (max-width: 768px) {
  .app-header-top {
    padding: 15px 15px;
    justify-content: center;
  }

  .app-header-top h1 {
    font-size: 22px;
    text-align: center;
    width: 100%;
  }

  .user-section {
    width: 100%;
    justify-content: center;
  }

  .app-nav {
    padding: 10px 15px;
    gap: 15px;
    justify-content: center;
  }

  .app-nav a {
    font-size: 14px;
  }
}
</style>