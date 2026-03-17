<script setup>
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Divider from 'primevue/divider';
import Tag from 'primevue/tag';

defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  device: {
    type: Object,
    default: null,
  },
  statusSeverity: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  'update:visible',
  'ping',
  'sync',
  'reboot',
  'delete',
  'upload-config',
]);

const closeDialog = () => {
  emit('update:visible', false);
};
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    header="Manage Device"
    :style="{ width: '720px' }"
    @update:visible="closeDialog"
  >
    <div v-if="device" class="manage-content">
      <div class="manage-header">
        <div>
          <h2>{{ device.name }}</h2>
          <p class="subtitle">{{ device.ip }}</p>
        </div>
        <Tag :severity="statusSeverity(device.status)" :value="device.status" />
      </div>

      <Divider />

      <div class="manage-grid">
        <div>
          <span class="label">Last Seen</span>
          <span>{{ device.lastSeen }}</span>
        </div>
        <div>
          <span class="label">Config</span>
          <span>Stored config · {{ device.config?.split('\n').length - 1 }} lines</span>
        </div>
      </div>

      <Divider />

      <div class="manage-actions">
        <Button label="Upload Config" severity="primary" @click="emit('upload-config')" />
        <Button label="Ping Device" severity="success" @click="emit('ping')" />
        <Button label="Sync Time" severity="info" @click="emit('sync')" />
        <Button label="Reboot" severity="warning" @click="emit('reboot')" />
        <Button label="Delete" severity="danger" @click="emit('delete')" />
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.manage-content {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.manage-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.manage-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

.manage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.5rem;
}

.manage-grid .label {
  display: block;
  color: #9ca3af;
  font-size: 0.85rem;
  margin-bottom: 0.35rem;
}

.manage-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

@media (max-width: 1024px) {
  .manage-grid {
    grid-template-columns: 1fr;
  }
}
</style>
