<template>
  <div class="container-fluid mt-4">
    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
      <h1 class="h3 mb-0 text-gray-800">Dashboard</h1>
    </div>

    <!-- Row 1: Batch Counts & Some Quartz Counts -->
    <div class="row">
      <StatCard 
        title="Total Batch Jobs" 
        :value="batchCountData?.batchCount ?? 0" 
        iconClass="fas fa-cubes" 
        borderColorClass="border-left-primary"
        :isLoading="!batchCountData"
      />
      <StatCard 
        title="Quartz Registered (Batch)" 
        :value="batchCountData?.registeredCount ?? 0" 
        iconClass="fas fa-link" 
        borderColorClass="border-left-success"
        :isLoading="!batchCountData"
      />
      <StatCard 
        title="Total Quartz Jobs" 
        :value="quartzCountData?.quartzCount ?? 0" 
        iconClass="fas fa-tasks" 
        borderColorClass="border-left-info"
        :isLoading="!quartzCountData"
      />
      <StatCard 
        title="Quartz Waiting" 
        :value="quartzCountData?.waitingCount ?? 0" 
        iconClass="fas fa-hourglass-half" 
        borderColorClass="border-left-warning"
        :isLoading="!quartzCountData"
      />
    </div>

    <!-- Row 2: Remaining Quartz Counts -->
    <div class="row">
      <StatCard 
        title="Quartz Acquired" 
        :value="quartzCountData?.acquiredCount ?? 0" 
        iconClass="fas fa-play-circle" 
        borderColorClass="border-left-secondary"
        :isLoading="!quartzCountData"
      />
      <StatCard 
        title="Quartz Paused" 
        :value="quartzCountData?.pausedCount ?? 0" 
        iconClass="fas fa-pause-circle" 
        borderColorClass="border-left-danger"
        :isLoading="!quartzCountData"
      />
      <StatCard 
        title="Quartz Blocked" 
        :value="quartzCountData?.blockedCount ?? 0" 
        iconClass="fas fa-ban" 
        borderColorClass="border-left-dark"
        :isLoading="!quartzCountData"
      />
      <!-- Add an empty card for alignment if needed, or adjust col counts if there are only 3 items -->
      <div class="col-xl-3 col-md-6 mb-4" v-if="false"></div> 
    </div>

    <!-- Row 3: Log Summaries (Pie Charts) -->
    <div class="row">
      <ChartCard
        title="Batch Job Logs (Today)"
        chartId="batchLogPieChart"
        :chartData="batchLogData"
        successLabel="Success"
        failureLabel="Failed"
      />
      <ChartCard
        title="Quartz Job Logs (Today)"
        chartId="quartzLogPieChart"
        :chartData="quartzLogData"
        successLabel="Success"
        failureLabel="Failed"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from '@/api/axios';
import StatCard from '@/components/dashboard/StatCard.vue';
import ChartCard from '@/components/dashboard/ChartCard.vue';

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
  failureCount: number; // Ensure this matches the backend response field name
}

// Reactive data properties
const batchCountData = ref<BatchCountResponse | null>(null);
const quartzCountData = ref<QuartzCountResponse | null>(null);
const batchLogData = ref<DailyStatusCountResponse | null>(null);
const quartzLogData = ref<DailyStatusCountResponse | null>(null);

// Fetching functions
async function fetchBatchCount() {
  try {
    const response = await axios.get<BatchCountResponse>('/dashboard/batchCount');
    batchCountData.value = response.data;
  } catch (error) {
    console.error('Error fetching batch count:', error);
    batchCountData.value = { batchCount: 0, registeredCount: 0 }; // Provide default on error
  }
}

async function fetchQuartzCount() {
  try {
    const response = await axios.get<QuartzCountResponse>('/dashboard/quartzCount');
    quartzCountData.value = response.data;
  } catch (error) {
    console.error('Error fetching quartz count:', error);
    quartzCountData.value = { quartzCount: 0, waitingCount: 0, acquiredCount: 0, pausedCount: 0, blockedCount: 0 }; // Default
  }
}

async function fetchBatchLogCount() {
  try {
    const response = await axios.get<DailyStatusCountResponse>('/dashboard/batchLogCount');
    batchLogData.value = response.data;
  } catch (error) {
    console.error('Error fetching batch log count:', error);
    batchLogData.value = { successCount: 0, failureCount: 0 }; // Default
  }
}

async function fetchQuartzLogCount() {
  try {
    const response = await axios.get<DailyStatusCountResponse>('/dashboard/quartzLogCount');
    quartzLogData.value = response.data;
  } catch (error) {
    console.error('Error fetching quartz log count:', error);
    quartzLogData.value = { successCount: 0, failureCount: 0 }; // Default
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
/* DashboardView specific styles can remain here if any */
/* .container-fluid { ... } */
</style>