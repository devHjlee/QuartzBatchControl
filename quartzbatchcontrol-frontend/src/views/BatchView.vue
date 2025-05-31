<template>
  <BatchTable />
</template>

<script lang="ts">
// Options API를 사용하여 컴포넌트 이름을 명시적으로 정의합니다.
// 이는 Vue Devtools 등에서 컴포넌트를 식별하는 데 도움이 될 수 있습니다.
export default {
  name: 'Batch' // 라우터 또는 다른 곳에서 참조될 수 있는 컴포넌트 이름
}
</script>

<script setup lang="ts">
/**
 * BatchView.vue
 * 배치 관리 기능을 제공하는 BatchTable 컴포넌트를 렌더링하는 뷰입니다.
 */
import BatchTable from '@/components/batch/BatchTable.vue'
</script>
