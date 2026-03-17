<script setup>
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  config: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(['update:visible', 'update:config', 'save']);

const closeDialog = () => {
  emit('update:visible', false);
};

const updateConfig = (value) => {
  emit('update:config', value);
};
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    header="Config Editor"
    :style="{ width: '680px' }"
    @update:visible="closeDialog"
  >
    <Textarea
      :model-value="config"
      autoResize
      rows="12"
      class="config-editor"
      @update:model-value="updateConfig"
    />
    <div class="dialog-actions">
      <Button label="Cancel" severity="secondary" @click="closeDialog" />
      <Button label="Save Config" severity="primary" @click="emit('save')" />
    </div>
  </Dialog>
</template>

<style scoped>
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.config-editor {
  width: 100%;
  margin-bottom: 1.25rem;
}
</style>
