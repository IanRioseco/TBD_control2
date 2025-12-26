<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import authService from '../services/authService';

const router = useRouter();

const username = ref('');
const firstname = ref('');
const lastname = ref('');
const password = ref('');
const address = ref('');
const latitude = ref('');
const longitude = ref('');
const loading = ref(false);
const error = ref('');

const onSubmit = async () => {
  loading.value = true;
  error.value = '';

  try {
    const userPayload = {
      username: username.value,
      firstname: firstname.value,
      lastname: lastname.value,
      password: password.value,
      address: address.value,
      latitude: latitude.value ? Number(latitude.value) : null,
      longitude: longitude.value ? Number(longitude.value) : null
    };

    // Usamos el servicio corregido
    await authService.register(userPayload);
    router.push({ name: 'tasks' });
  } catch (e) {
    error.value = 'Error al registrar usuario.';
    console.error(e);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="auth-container">
    <h2>Registrarse</h2>

    <form @submit.prevent="onSubmit" class="auth-form">
      <label>
        Nombre de usuario
        <input v-model="username" type="text" required />
      </label>

      <label>
        Nombre
        <input v-model="firstname" type="text" required />
      </label>

      <label>
        Apellido
        <input v-model="lastname" type="text" required />
      </label>

      <label>
        Contraseña
        <input v-model="password" type="password" required />
      </label>

      <label>
        Dirección
        <input v-model="address" type="text" placeholder="Calle, número, ciudad..." />
      </label>

      <label>
        Latitud
        <input v-model="latitude" type="number" step="0.000001" />
      </label>

      <label>
        Longitud
        <input v-model="longitude" type="number" step="0.000001" />
      </label>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Creando cuenta...' : 'Registrarse' }}
      </button>

      <p v-if="error" class="error">{{ error }}</p>
    </form>

    <p class="switch-link">
      ¿Ya tienes cuenta?
      <router-link to="/login">Inicia sesión aquí</router-link>
    </p>
  </div>
</template>

<style scoped>
/* Agrega aquí los estilos CSS si los deseas, similares a LoginView */
.auth-container { max-width: 400px; margin: 40px auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
.auth-form { display: flex; flex-direction: column; gap: 10px; }
.auth-form label { display: flex; flex-direction: column; text-align: left; }
.error { color: red; }
.switch-link { margin-top: 10px; }
</style>