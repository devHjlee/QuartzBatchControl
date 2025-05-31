<template>
  <BatchLogTable />
</template>

<script lang="ts">
// Options API를 사용하여 컴포넌트 이름을 명시적으로 정의합니다.
export default {
  name: 'BatchLogView' // 라우터 또는 다른 곳에서 참조될 수 있는 컴포넌트 이름
}
</script>

<script setup lang="ts">
/**
 * BatchLogView.vue
 * 배치 실행 로그를 보여주는 BatchLogTable 컴포넌트를 렌더링하는 뷰입니다.
 */
import BatchLogTable from '@/components/batch/BatchLogTable.vue'
</script>
