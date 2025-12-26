<template>
  <div class="map-container">
    <div ref="mapEl" class="map"></div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix íconos Leaflet en Vite
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const props = defineProps({
  tasks: { type: Array, default: () => [] },
  sectors: { type: Array, default: () => [] },
  userLocation: { type: Object, default: null }, // {latitude, longitude}
});

const mapEl = ref(null);

let map = null;
let layer = null;

const sectorById = (id) => props.sectors.find(s => s.id === id);

const taskColor = (t) => {
  if (t.finished) return '#90EE90';
  if (t.important) return '#FF6B6B';
  return '#FFD700';
};

const divDotIcon = (color) =>
  L.divIcon({
    className: '',
    html: `
      <div style="
        width: 14px; height: 14px;
        border-radius: 50%;
        background: ${color};
        border: 2px solid #222;
        box-shadow: 0 0 6px rgba(0,0,0,0.35);
      "></div>
    `,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
  });

const renderMap = () => {
  if (!map || !layer) return;

  layer.clearLayers();

  const bounds = [];

  // 1) Marcador usuario/admin
  if (props.userLocation?.latitude != null && props.userLocation?.longitude != null) {
    const lat = props.userLocation.latitude;
    const lon = props.userLocation.longitude;

    const userMarker = L.marker([lat, lon], {
      icon: L.divIcon({
        className: '',
        html: `
          <div style="
            width: 18px; height: 18px;
            border-radius: 50%;
            background: #4CAF50;
            border: 3px solid #1a1a1a;
            box-shadow: 0 0 10px rgba(0,0,0,0.4);
          "></div>
        `,
        iconSize: [18, 18],
        iconAnchor: [9, 9],
      })
    }).bindPopup('<b>Tu ubicación</b>');

    userMarker.addTo(layer);
    bounds.push([lat, lon]);
  }

  // 2) Marcadores de sectores (azul)
  props.sectors.forEach(s => {
    if (s.latitude == null || s.longitude == null) return;

    const m = L.circleMarker([s.latitude, s.longitude], {
      radius: 6,
      color: '#2196F3',
      fillColor: '#2196F3',
      fillOpacity: 0.8,
      weight: 2,
    }).bindPopup(`<b>Sector:</b> ${s.name}`);

    m.addTo(layer);
    bounds.push([s.latitude, s.longitude]);
  });

  // 3) Marcadores de tareas (según estado), ubicadas en el sector de la tarea
  props.tasks.forEach(t => {
    const sec = sectorById(t.sectorId);
    if (!sec || sec.latitude == null || sec.longitude == null) return;

    const icon = divDotIcon(taskColor(t));

    const popup = `
      <div style="min-width:220px">
        <b>${t.title}</b><br/>
        <div style="margin-top:6px">${t.description ?? ''}</div>
        <hr style="border:none;border-top:1px solid #ddd;margin:8px 0"/>
        <div><b>Sector:</b> ${t.sectorName ?? sec.name}</div>
        <div><b>Vence:</b> ${t.dueDate ?? '-'}</div>
        <div><b>Estado:</b> ${t.finished ? 'Completada' : 'Pendiente'}</div>
        <div><b>Importante:</b> ${t.important ? 'Sí ⭐' : 'No'}</div>
      </div>
    `;

    L.marker([sec.latitude, sec.longitude], { icon })
      .bindPopup(popup)
      .addTo(layer);

    bounds.push([sec.latitude, sec.longitude]);
  });

  // Ajustar vista
  if (bounds.length) {
    map.fitBounds(bounds, { padding: [30, 30] });
  } else {
    // fallback (Santiago)
    map.setView([-33.45, -70.65], 12);
  }
};

onMounted(() => {
  map = L.map(mapEl.value, {
    zoomControl: true,
    preferCanvas: true,
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map);

  layer = L.layerGroup().addTo(map);

  renderMap();
});

watch(
  () => [props.tasks, props.sectors, props.userLocation],
  () => renderMap(),
  { deep: true }
);

onBeforeUnmount(() => {
  if (map) map.remove();
  map = null;
  layer = null;
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
}

.map {
  width: 100%;
  height: 100%;
  min-height: 600px;
}
</style>
