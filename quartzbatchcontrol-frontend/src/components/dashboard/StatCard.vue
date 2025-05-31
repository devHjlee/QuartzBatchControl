<template>
  <div class="col-xl-3 col-md-6 mb-4">
    <div :class="['card', borderColorClass, 'shadow', 'h-100', 'py-2']">
      <div class="card-body">
        <div class="row no-gutters align-items-center">
          <div class="col mr-2">
            <div :class="['text-xs', 'font-weight-bold', dynamicTextColorClass, 'text-uppercase', 'mb-1']">
              {{ title }}
            </div>
            <div class="h5 mb-0 font-weight-bold text-gray-800">
              {{ isLoading ? '로딩중...' : value }}
            </div>
          </div>
          <div class="col-auto">
            <i :class="[iconClass, 'fa-2x', 'text-gray-300']"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  iconClass: {
    type: String,
    required: true
  },
  borderColorClass: {
    type: String,
    default: 'border-left-primary' // 예: border-left-primary, border-left-success
  },
  isLoading: {
    type: Boolean,
    default: false
  }
});

/**
 * borderColorClass prop을 기반으로 동적 텍스트 색상 클래스를 계산합니다.
 * 예: 'border-left-primary' -> 'text-primary'
 */
const dynamicTextColorClass = computed(() => {
  const colorName = props.borderColorClass.replace('border-left-', '');
  if (['primary', 'success', 'info', 'warning', 'danger', 'secondary', 'dark'].includes(colorName)) {
    return `text-${colorName}`;
  }
  return 'text-primary'; // 매칭되는 색상이 없으면 기본값으로 'text-primary' 사용
});
</script>

<style scoped>
/* StatCard 특정 스타일 */
.card .card-body {
  padding: 1.25rem; /* SB Admin 2 기본값과 유사하게 조정 */
}
.text-xs {
  font-size: 0.7rem; /* SB Admin 2 기본값과 유사하게 조정 */
}
</style> 