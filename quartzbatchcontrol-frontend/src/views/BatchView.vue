<template>
  <div class="container-fluid">
    <h1 class="h3 mb-2 text-gray-800">Batch 목록</h1>
    <p class="mb-4">등록된 Batch 실행 상태 및 정보를 확인하세요.</p>
    <BatchTable />
    <div class="card shadow mb-4">

    </div>
  </div>
</template>

<script setup lang="ts">
import BatchTable from '@/components/batch/BatchTable.vue'
</script>
