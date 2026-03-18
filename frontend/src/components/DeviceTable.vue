<script setup>
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Tag from 'primevue/tag';

defineProps({
  devices: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  statusSeverity: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits(['manage']);

const handleRowClick = (event) => {
  emit('manage', event.data);
};

const handleManageClick = (device) => {
  emit('manage', device);
};
</script>

<template>
  <DataTable
    :value="devices"
    dataKey="id"
    stripedRows
    rowHover
    :loading="loading"
    emptyMessage="No devices available."
    @row-click="handleRowClick"
  >
    <Column field="name" header="Device" />
    <Column header="Status">
      <template #body="{ data }">
        <Tag :severity="statusSeverity(data.status)" :value="data.status" />
      </template>
    </Column>
    <Column header="Manage" style="width: 140px">
      <template #body="{ data }">
        <Button label="Manage" size="small" @click.stop="handleManageClick(data)" />
      </template>
    </Column>
  </DataTable>
</template>
