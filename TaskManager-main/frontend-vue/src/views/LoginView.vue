<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import authService from '../services/authService';

const router = useRouter();

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const onSubmit = async () => {
  loading.value = true;
  error.value = '';

  try {
    // Usamos el servicio que ya tiene la ruta corregida (/api/auth/login)
    await authService.login(username.value, password.value);
    router.push({ name: 'tasks' });
  } catch (e) {
    console.error(e);
    error.value = 'Usuario o contraseña incorrectos.';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">Iniciar sesión</h2>

      <form @submit.prevent="onSubmit" class="auth-form">
        <label class="auth-label">
          Nombre de usuario
          <input
            v-model="username"
            type="text"
            required
            class="auth-input"
            autocomplete="username"
          />
        </label>

        <label class="auth-label">
          Contraseña
          <input
            v-model="password"
            type="password"
            required
            class="auth-input"
            autocomplete="current-password"
          />
        </label>

        <button type="submit" class="auth-button" :disabled="loading">
          {{ loading ? 'Ingresando...' : 'Entrar' }}
        </button>

        <p v-if="error" class="auth-error">
          {{ error }}
        </p>
      </form>

      <p class="auth-switch">
        ¿No tienes cuenta?
        <router-link to="/register">Regístrate aquí</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* Estilos iguales al original */
.auth-page { min-height: 100vh; display: flex; justify-content: center; align-items: center; background: #f3f4f6; padding: 16px; }
.auth-card { width: 360px; max-width: 100%; background: #ffffff; border-radius: 12px; padding: 24px 24px 28px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08); }
.auth-title { font-size: 20px; font-weight: 700; text-align: center; margin-bottom: 20px; }
.auth-form { display: flex; flex-direction: column; gap: 12px; }
.auth-label { display: flex; flex-direction: column; font-size: 13px; color: #374151; }
.auth-input { margin-top: 4px; border-radius: 8px; border: 1px solid #d1d5db; padding: 8px 10px; font-size: 14px; outline: none; }
.auth-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.35); }
.auth-button { margin-top: 6px; border: none; border-radius: 8px; padding: 9px 10px; font-size: 14px; font-weight: 600; background: #2563eb; color: white; cursor: pointer; transition: background 0.15s ease; }
.auth-button:disabled { opacity: 0.7; cursor: default; }
.auth-button:not(:disabled):hover { background: #1d4ed8; }
.auth-error { margin-top: 6px; font-size: 13px; color: #b91c1c; }
.auth-switch { margin-top: 16px; font-size: 13px; text-align: center; color: #4b5563; }
</style>