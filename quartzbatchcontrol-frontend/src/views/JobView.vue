<template>
  <JobTable />
</template>

<script lang="ts">
export default {
  name: 'Job' // 라우터에 정의된 name과 일치
}
</script>

<script setup lang="ts">
import JobTable from '@/components/jobs/JobTable.vue'
</script>
