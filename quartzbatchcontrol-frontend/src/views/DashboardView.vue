<template>
  <div class="container-fluid mt-4">
    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
      <h1 class="h3 mb-0 text-gray-800">Dashboard</h1>
    </div>

    <!-- Row 1: Batch Counts & Some Quartz Counts -->
    <div class="row">
      <!-- Batch Total Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-primary shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                  Total Batch Jobs
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ batchCountData?.batchCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-cubes fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Batch Registered with Quartz Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-success shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                  Quartz Registered (Batch)
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ batchCountData?.registeredCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-link fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quartz Total Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                  Total Quartz Jobs
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ quartzCountData?.quartzCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-tasks fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quartz Waiting Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-warning shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                  Quartz Waiting
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ quartzCountData?.waitingCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-hourglass-half fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 2: Remaining Quartz Counts -->
    <div class="row">
      <!-- Quartz Acquired Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-secondary shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-secondary text-uppercase mb-1">
                  Quartz Acquired
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ quartzCountData?.acquiredCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-play-circle fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quartz Paused Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-danger shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-danger text-uppercase mb-1">
                  Quartz Paused
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ quartzCountData?.pausedCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-pause-circle fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Quartz Blocked Card -->
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-dark shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-dark text-uppercase mb-1">
                  Quartz Blocked
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ quartzCountData?.blockedCount ?? 'Loading...' }}
                </div>
              </div>
              <div class="col-auto">
                <i class="fas fa-ban fa-2x text-gray-300"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 3: Log Summaries (Pie Charts) -->
    <div class="row">
      <!-- Batch Log Pie Chart -->
      <div class="col-lg-6 mb-4">
        <div class="card shadow">
          <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
            <h6 class="m-0 font-weight-bold text-primary">Batch Job Logs (Today)</h6>
          </div>
          <div class="card-body">
            <div class="chart-pie pt-4 pb-2">
              <canvas id="batchLogPieChart"></canvas>
            </div>
            <div class="mt-4 text-center small">
              <span class="mr-2">
                <i class="fas fa-circle text-success"></i> Success
              </span>
              <span class="mr-2">
                <i class="fas fa-circle text-danger"></i> Failed
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quartz Log Pie Chart -->
      <div class="col-lg-6 mb-4">
        <div class="card shadow">
          <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
            <h6 class="m-0 font-weight-bold text-primary">Quartz Job Logs (Today)</h6>
            </div>
          <div class="card-body">
            <div class="chart-pie pt-4 pb-2">
              <canvas id="quartzLogPieChart"></canvas>
            </div>
            <div class="mt-4 text-center small">
              <span class="mr-2">
                <i class="fas fa-circle text-success"></i> Success
              </span>
              <span class="mr-2">
                <i class="fas fa-circle text-danger"></i> Failed
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import axios from '@/api/axios';
// Chart.js is now expected to be globally available via <script> tag in index.html

// Define interfaces for API responses
interface BatchCountResponse {
  batchCount: number;
  registeredCount: number;
}

interface QuartzCountResponse {
  quartzCount: number;
  waitingCount: number;
  acquiredCount: number;
  pausedCount: number;
  blockedCount: number;
}

interface DailyStatusCountResponse {
  successCount: number; 
  failureCount: number;
}

// Simple interface for Chart.js instance to satisfy TypeScript
interface ChartInstance {
  destroy: () => void;
  // Add other methods/properties if they are directly called on the instance
}

// Reactive data properties
const batchCountData = ref<BatchCountResponse | null>(null);
const quartzCountData = ref<QuartzCountResponse | null>(null);
const batchLogData = ref<DailyStatusCountResponse | null>(null);
const quartzLogData = ref<DailyStatusCountResponse | null>(null);

// Refs for chart instances
const batchLogPieChartInstance = ref<ChartInstance | null>(null);
const quartzLogPieChartInstance = ref<ChartInstance | null>(null);

// const API_BASE_URL = '/api/dashboard'; // No longer needed, baseURL is in @/api/axios

// Fetching functions
async function fetchBatchCount() {
  try {
    const response = await axios.get<BatchCountResponse>('/dashboard/batchCount');
    batchCountData.value = response.data;
  } catch (error) {
    console.error('Error fetching batch count:', error);
  }
}

async function fetchQuartzCount() {
  try {
    const response = await axios.get<QuartzCountResponse>('/dashboard/quartzCount');
    quartzCountData.value = response.data;
  } catch (error) {
    console.error('Error fetching quartz count:', error);
  }
}

async function fetchBatchLogCount() {
  try {
    const response = await axios.get<DailyStatusCountResponse>('/dashboard/batchLogCount');
    batchLogData.value = response.data;
    await nextTick();
    renderBatchLogPieChart();
  } catch (error) {
    console.error('Error fetching batch log count:', error);
  }
}

async function fetchQuartzLogCount() {
  try {
    const response = await axios.get<DailyStatusCountResponse>('/dashboard/quartzLogCount');
    quartzLogData.value = response.data;
    await nextTick();
    renderQuartzLogPieChart();
  } catch (error) {
    console.error('Error fetching quartz log count:', error);
  }
}

// Chart rendering functions
function createPieChartConfig(data: number[], labels: string[]): any { // Chart.js config can be complex, using 'any' for simplicity here
  return {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: ['#1cc88a', '#e74a3b', '#f6c23e', '#36b9cc', '#5a5c69'],
        hoverBackgroundColor: ['#17a673', '#c73e30', '#d4a120', '#2c9faf', '#43444e'],
        hoverBorderColor: "rgba(234, 236, 244, 1)",
      }],
    },
    options: {
      maintainAspectRatio: false,
      responsive: true,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: "rgb(255,255,255)",
          bodyColor: "#858796",
          borderColor: '#dddfeb',
          borderWidth: 1,
          callbacks: {
              label: function(context: any) { // context type can also be made more specific if needed
                  let label = context.label || '';
                  if (label) {
                      label += ': ';
                  }
                  if (context.parsed !== null && context.parsed !== undefined) { // Check for undefined as well
                      label += context.parsed;
                  }
                  return label;
              }
          }
        }
      },
      cutout: '80%',
    },
  };
}

function renderBatchLogPieChart() {
  if (!(window as any).Chart) {
    console.error("Chart.js is not loaded globally.");
    return;
  }
  if (batchLogPieChartInstance.value) {
    batchLogPieChartInstance.value.destroy();
  }
  const canvas = document.getElementById('batchLogPieChart') as HTMLCanvasElement;
  if (canvas && batchLogData.value) {
    const config = createPieChartConfig(
      [batchLogData.value.successCount, batchLogData.value.failureCount],
      ['Success', 'Failed']
    );
    batchLogPieChartInstance.value = new (window as any).Chart(canvas.getContext('2d')!, config);
  }
}

function renderQuartzLogPieChart() {
  if (!(window as any).Chart) {
    console.error("Chart.js is not loaded globally.");
    return;
  }
  if (quartzLogPieChartInstance.value) {
    quartzLogPieChartInstance.value.destroy();
  }
  const canvas = document.getElementById('quartzLogPieChart') as HTMLCanvasElement;
  if (canvas && quartzLogData.value) {
    const config = createPieChartConfig(
      [quartzLogData.value.successCount, quartzLogData.value.failureCount],
      ['Success', 'Failed']
    );
    quartzLogPieChartInstance.value = new (window as any).Chart(canvas.getContext('2d')!, config);
  }
}

// Fetch all data on component mount
onMounted(async () => {
  await fetchBatchCount();
  await fetchQuartzCount();
  await fetchBatchLogCount();
  await fetchQuartzLogCount();
});

</script>

<style scoped>
.chart-pie {
  position: relative;
  height: 15rem;
  width: 100%;
}

.card-body .fas {
}
</style>