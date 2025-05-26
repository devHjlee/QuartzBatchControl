<template>
  <div class="container mt-5">
    <h2>Dashboard</h2>
    <p>로그인 성공! 여기에 대시보드 내용을 추가하세요.</p>
  </div>
</template>

<script lang="ts">
export default {
  name: 'Dashboard' // 라우터에 정의된 name과 일치
}
</script>

<script setup lang="ts">
// 필요시 스크립트 작성
import Dashboard from '@/components/dashboard/Dashboard.vue';
</script>