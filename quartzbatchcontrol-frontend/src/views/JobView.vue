<template>
  <div class="container-fluid">
    <h1 class="h3 mb-2 text-gray-800">Quartz Job 목록</h1>
    <p class="mb-4">등록된 잡의 실행 상태 및 정보를 확인하세요.</p>

    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">Job Table</h6>
      </div>
      <div class="card-body">
        <JobTable />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import JobTable from '@/components/jobs/JobTable.vue'
</script>
