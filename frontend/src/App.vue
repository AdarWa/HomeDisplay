<script setup>
import { computed, onMounted, ref } from 'vue';
import Card from 'primevue/card';
import Message from 'primevue/message';
import ConfigDialog from '@/components/ConfigDialog.vue';
import DashboardHeader from '@/components/DashboardHeader.vue';
import DeviceTable from '@/components/DeviceTable.vue';
import ManageDialog from '@/components/ManageDialog.vue';
import {
  deleteDevice as deleteDeviceRequest,
  fetchDevices,
  pingDevice as pingDeviceRequest,
  rebootDevice as rebootDeviceRequest,
  syncDevice as syncDeviceRequest,
  updateDeviceConfig,
} from '@/api/devices.js';

const devices = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');

const manageVisible = ref(false);
const configVisible = ref(false);
const selectedDevice = ref(null);
const configDraft = ref('');

const onlineCount = computed(() => devices.value.filter((device) => device.status === 'Online').length);
const offlineCount = computed(() => devices.value.filter((device) => device.status === 'Offline').length);

const statusSeverity = (status) => {
  switch (status) {
    case 'Online':
      return 'success';
    case 'Offline':
      return 'danger';
    default:
      return 'info';
  }
};

const openManage = (device) => {
  selectedDevice.value = device;
  manageVisible.value = true;
};

const updateSelectedDevice = (updates) => {
  if (!selectedDevice.value) {
    return;
  }

  const index = devices.value.findIndex((device) => device.id === selectedDevice.value.id);
  if (index === -1) {
    return;
  }

  const updated = { ...devices.value[index], ...updates };
  devices.value[index] = updated;
  selectedDevice.value = updated;
};

const runWithError = async (action) => {
  errorMessage.value = '';
  try {
    return await action();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Request failed.';
    return null;
  }
};

const pingDevice = async () => {
  if (!selectedDevice.value) {
    return;
  }

  const updated = await runWithError(() => pingDeviceRequest(selectedDevice.value.id));
  if (!updated && errorMessage.value) {
    return;
  }
  updateSelectedDevice(updated || { status: 'Online', lastSeen: new Date().toLocaleString() });
};

const rebootDevice = async () => {
  if (!selectedDevice.value) {
    return;
  }

  const updated = await runWithError(() => rebootDeviceRequest(selectedDevice.value.id));
  if (!updated && errorMessage.value) {
    return;
  }
  updateSelectedDevice(updated || { status: 'Error', lastSeen: 'Rebooting now' });
};

const syncDevice = async () => {
  if (!selectedDevice.value) {
    return;
  }

  const updated = await runWithError(() => syncDeviceRequest(selectedDevice.value.id));
  if (!updated && errorMessage.value) {
    return;
  }
  updateSelectedDevice(updated || { lastSeen: `Synced ${new Date().toLocaleString()}` });
};

const deleteDevice = async () => {
  if (!selectedDevice.value) {
    return;
  }

  const success = await runWithError(() => deleteDeviceRequest(selectedDevice.value.id));
  if (errorMessage.value) {
    return;
  }

  if (success !== null) {
    devices.value = devices.value.filter((device) => device.id !== selectedDevice.value.id);
  }
  manageVisible.value = false;
  selectedDevice.value = null;
};

const openConfigEditor = () => {
  configDraft.value = selectedDevice.value?.config || '# Device Config\n';
  configVisible.value = true;
};

const saveConfig = async () => {
  if (!selectedDevice.value) {
    return;
  }

  const updated = await runWithError(() =>
    updateDeviceConfig(selectedDevice.value.id, configDraft.value),
  );
  if (errorMessage.value) {
    return;
  }

  updateSelectedDevice(
    updated || {
      config: configDraft.value,
      lastSeen: `Config updated ${new Date().toLocaleString()}`,
    },
  );
  configVisible.value = false;
};

const loadDevices = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    devices.value = await fetchDevices();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load devices.';
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadDevices);
</script>

<template>
  <div class="dashboard">
    <DashboardHeader />

    <section class="dashboard-grid">
      <Card class="device-card">
        <template #title>Devices</template>
        <template #subtitle>Click a device row or manage button to open actions.</template>
        <template #content>
          <Message v-if="errorMessage" severity="error" class="error-message">
            {{ errorMessage }}
          </Message>
          <DeviceTable
            :devices="devices"
            :status-severity="statusSeverity"
            :loading="isLoading"
            @manage="openManage"
          />
        </template>
      </Card>

    </section>

    <ManageDialog
      v-model:visible="manageVisible"
      :device="selectedDevice"
      :status-severity="statusSeverity"
      @ping="pingDevice"
      @sync="syncDevice"
      @reboot="rebootDevice"
      @delete="deleteDevice"
      @upload-config="openConfigEditor"
    />

    <ConfigDialog
      v-model:visible="configVisible"
      v-model:config="configDraft"
      @save="saveConfig"
    />
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.device-card :deep(.p-card-body) {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.error-message {
  margin-bottom: 0.5rem;
}
</style>
