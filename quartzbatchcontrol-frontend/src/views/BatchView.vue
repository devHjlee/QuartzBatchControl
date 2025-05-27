<template>
  <BatchTable />
</template>

<script lang="ts">
export default {
  name: 'Batch' // 라우터에 정의된 name과 일치
}
</script>

<script setup lang="ts">
import BatchTable from '@/components/batch/BatchTable.vue'
</script>
