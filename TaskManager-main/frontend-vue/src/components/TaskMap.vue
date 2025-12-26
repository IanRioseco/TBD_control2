<template>
  <div class="map-container">
    <div id="map" class="map"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const props = defineProps({
  tasks: { type: Array, default: () => [] },
  sectors: { type: Array, default: () => [] },
  userLocation: { type: Object, default: null }
});

let map = null;

const initMap = () => {
  // Crear mapa centrado en Santiago, Chile
  map = L.map('map').setView([-33.4429, -70.6539], 12);

  // Agregar capa de OpenStreetMap
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
  }).addTo(map);

  // Agregar marcador del usuario
  if (props.userLocation && props.userLocation.latitude && props.userLocation.longitude) {
    L.circleMarker([props.userLocation.latitude, props.userLocation.longitude], {
      radius: 10,
      fillColor: '#4CAF50',
      color: '#2E7D32',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.8
    })
    .addTo(map)
    .bindPopup('<strong>Tu ubicación</strong>');
  }

  // Agregar marcadores de sectores
  props.sectors.forEach(sector => {
    if (sector.latitude && sector.longitude) {
      L.circleMarker([sector.latitude, sector.longitude], {
        radius: 8,
        fillColor: '#2196F3',
        color: '#1565C0',
        weight: 2,
        opacity: 1,
        fillOpacity: 0.7
      })
      .addTo(map)
      .bindPopup(`<strong>${sector.name}</strong>`);
    }
  });

  // Agregar marcadores de tareas
  props.tasks.forEach(task => {
    const sectorData = props.sectors.find(s => s.id === task.sectorId);
    if (sectorData && sectorData.latitude && sectorData.longitude) {
      const iconColor = task.finished ? '#90EE90' : (task.important ? '#FF6B6B' : '#FFD700');
      
      L.circleMarker([sectorData.latitude, sectorData.longitude], {
        radius: 6,
        fillColor: iconColor,
        color: '#333',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.9
      })
      .addTo(map)
      .bindPopup(`
        <div class="popup-content">
          <strong>${task.title}</strong><br/>
          <small>${task.description}</small><br/>
          <small>📍 ${sectorData.name}</small><br/>
          <small>📅 Vence: ${formatDate(task.dueDate)}</small><br/>
          <small>${task.finished ? '✓ Completada' : '⏳ Pendiente'}</small>
        </div>
      `);
    }
  });

  // Ajustar zoom automático si hay tareas
  if (props.tasks.length > 0) {
    const bounds = L.latLngBounds(
      props.tasks
        .map(task => {
          const sector = props.sectors.find(s => s.id === task.sectorId);
          return sector ? [sector.latitude, sector.longitude] : null;
        })
        .filter(Boolean)
    );
    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('es-CL', { timeZone: 'UTC' });
};

onMounted(() => {
  initMap();
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

#map {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.popup-content {
  font-size: 12px;
  color: #333;
}

.popup-content strong {
  display: block;
  margin-bottom: 5px;
  color: #000;
}

.popup-content small {
  display: block;
  line-height: 1.5;
}
</style>
