<template>
  <div class="col-lg-6 mb-4">
    <div class="card shadow">
      <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
        <h6 class="m-0 font-weight-bold text-primary">{{ title }}</h6>
      </div>
      <div class="card-body">
        <div class="chart-pie pt-4 pb-2">
          <canvas :id="chartId"></canvas>
        </div>
        <div class="mt-4 text-center small">
          <span class="mr-2">
            <i class="fas fa-circle text-success"></i> {{ successLabel }}
          </span>
          <span class="mr-2">
            <i class="fas fa-circle text-danger"></i> {{ failureLabel }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import type { PropType } from 'vue';

/**
 * Chart.js 인스턴스에 대한 타입 정의.
 * Chart.js가 전역으로 로드되어 사용된다고 가정합니다.
 */
interface ChartJSInstance {
  destroy: () => void;
  update: (config?: any) => void; // update 메서드도 자주 사용되므로 추가
  // 필요한 다른 Chart.js 메서드나 속성을 여기에 추가할 수 있습니다.
}

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  chartId: {
    type: String,
    required: true
  },
  // 차트 데이터 구조: 성공 및 실패 횟수 또는 null
  chartData: {
    type: [Object, null] as PropType<{ successCount: number; failureCount: number } | null>,
    required: true
  },
  successLabel: {
    type: String,
    default: 'Success'
  },
  failureLabel: {
    type: String,
    default: 'Failed'
  }
});

const chartInstance = ref<ChartJSInstance | null>(null);

/**
 * Doughnut 차트 설정을 생성합니다.
 * @param data 차트에 표시될 데이터 배열 (예: [successCount, failureCount])
 * @param labels 각 데이터 조각에 대한 레이블 배열
 */
const createPieChartConfig = (data: number[], labels: string[]): any => {
  return {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: ['#1cc88a', '#e74a3b'], // Bootstrap Success, Danger
        hoverBackgroundColor: ['#17a673', '#c73e30'],
        hoverBorderColor: "rgba(234, 236, 244, 1)",
      }],
    },
    options: {
      maintainAspectRatio: false,
      responsive: true,
      plugins: {
        legend: {
          display: false // 범례 숨김
        },
        tooltip: {
          backgroundColor: "rgb(255,255,255)",
          bodyColor: "#858796",
          borderColor: '#dddfeb',
          borderWidth: 1,
          callbacks: {
            label: function(context: any) { // 툴클 레이블 포맷팅
              let label = context.label || '';
              if (label) {
                label += ': ';
              }
              if (context.parsed !== null && context.parsed !== undefined) {
                label += context.parsed;
              }
              return label;
            }
          }
        }
      },
      cutout: '80%', // 도넛 차트의 중앙 빈 공간 크기
    },
  };
};

/**
 * 차트를 렌더링하거나 업데이트합니다.
 * 데이터가 유효하면 새 차트를 그리거나 기존 차트를 업데이트합니다.
 * 데이터가 없으면 기존 차트를 파괴합니다.
 */
const renderOrUpdateChart = async () => {
  await nextTick(); // DOM 업데이트를 기다립니다.

  const ChartJS = (window as any).Chart;
  if (!ChartJS) {
    // Chart.js가 로드되지 않은 경우, 개발 환경에서는 콘솔에 오류를 남길 수 있습니다.
    // console.error("Chart.js is not loaded globally.");
    return;
  }

  const canvas = document.getElementById(props.chartId) as HTMLCanvasElement | null;
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  // 기존 차트 인스턴스가 있고 데이터가 유효하면 업데이트, 그렇지 않으면 파괴
  if (chartInstance.value) {
    chartInstance.value.destroy();
    chartInstance.value = null; // 참조 제거
  }

  if (props.chartData && (props.chartData.successCount > 0 || props.chartData.failureCount > 0)) {
    const config = createPieChartConfig(
      [props.chartData.successCount, props.chartData.failureCount],
      [props.successLabel, props.failureLabel]
    );
    chartInstance.value = new ChartJS(ctx, config);
  }
  // 데이터가 없거나 모든 값이 0인 경우, 차트를 그리지 않거나 (이미 위에서 파괴됨) 빈 상태로 둡니다.
};

// chartData prop이 변경될 때 차트 다시 렌더링
watch(() => props.chartData,
  (newData) => {
    renderOrUpdateChart();
  },
  { deep: true, immediate: false } // 초기 마운트 시에는 onMounted에서 처리
);

// 컴포넌트 마운트 시 초기 차트 렌더링
onMounted(() => {
  // 초기 데이터가 있거나, 데이터가 로드될 것을 예상하여 watch의 immediate 없이 여기서 호출
  renderOrUpdateChart();
});

</script>

<style scoped>
.chart-pie {
  position: relative;
  height: 15rem; /* SB Admin 2 기본 파이 차트 높이 */
  width: 100%;
}
</style> 