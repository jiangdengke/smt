<script setup>
defineProps({
  form: {
    type: Object,
    required: true
  },
  shiftOptions: {
    type: Array,
    required: true
  },
  factories: {
    type: Array,
    required: true
  },
  workshops: {
    type: Array,
    required: true
  },
  lines: {
    type: Array,
    required: true
  },
  headerLocked: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <n-card size="small" style="margin-bottom: 12px;">
    <n-space align="center" wrap>
      <span style="font-size: 13px; color: gray;">日期:</span>
      <n-date-picker
        v-model:formatted-value="form.prodDate"
        value-format="yyyy-MM-dd"
        type="date"
        size="small"
        :disabled="headerLocked"
        style="width: 130px"
      />
      <span style="font-size: 13px; color: gray;">班别:</span>
      <n-select
        v-model:value="form.shift"
        :options="shiftOptions"
        size="small"
        :disabled="headerLocked"
        style="width: 100px"
      />
      <span style="font-size: 13px; color: gray;">厂区:</span>
      <n-select
        v-model:value="form.factoryId"
        :options="factories"
        label-field="name"
        value-field="id"
        size="small"
        :disabled="headerLocked"
        style="width: 140px"
        placeholder="请选择"
      />
      <span style="font-size: 13px; color: gray;">车间:</span>
      <n-select
        v-model:value="form.workshopId"
        :options="workshops"
        label-field="name"
        value-field="id"
        size="small"
        :disabled="headerLocked"
        style="width: 140px"
        placeholder="请选择"
      />
      <span style="font-size: 13px; color: gray;">线别:</span>
      <n-select
        v-model:value="form.lineId"
        :options="lines"
        label-field="name"
        value-field="id"
        size="small"
        :disabled="headerLocked"
        style="width: 140px"
        placeholder="请选择"
      />
    </n-space>
  </n-card>
</template>
