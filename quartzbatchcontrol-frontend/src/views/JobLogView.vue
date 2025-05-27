<template>
  <JobLogTable />
</template>

<script lang="ts">
export default {
  name: 'JobLogView' // 라우터에 정의된 name과 일치
}
</script>

<script setup lang="ts">
import JobLogTable from '@/components/jobs/JobLogTable.vue'
</script>
