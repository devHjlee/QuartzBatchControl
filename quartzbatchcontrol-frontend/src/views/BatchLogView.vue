<template>
  <BatchLogTable />
</template>

<script lang="ts">
export default {
  name: 'BatchLogView' // 라우터에 정의된 name과 일치
}
</script>

<script setup lang="ts">
import BatchLogTable from '@/components/batch/BatchLogTable.vue'
</script>
