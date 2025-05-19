<template>
  <div>
    <!-- 🔍 검색 필드 -->
    <div class="mb-3 d-flex gap-2">
      <input v-model="search.jobName" class="form-control w-auto" placeholder="Job Name" />
      <input v-model="search.jobGroup" class="form-control w-auto" placeholder="Job Group" />
      <button class="btn btn-primary" @click="fetchJobs">검색</button>
    </div>

    <div class="table-responsive">
      <table class="table table-bordered align-middle text-center" width="100%" cellspacing="0">
        <thead>
          <tr>
            <th class="col-1 col-md-1 col-lg-1">Job Name</th>
            <th class="col-1 col-md-1 col-lg-1">Group</th>
            <th class="col-1 col-md-1 col-lg-1">Status</th>
            <th>Cron</th>
            <th>Next Run</th>
            <th>Last Run</th>
            <th class="col-1 col-md-1 col-lg-1">Type</th>
            <th class="col-1 col-md-1 col-lg-1">Event</th>
            <th>Created By</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="job in jobs" :key="job.jobName + job.jobGroup">
            <td class="col-1 col-md-1 col-lg-1">{{ job.jobName }}</td>
            <td class="col-1 col-md-1 col-lg-1">{{ job.jobGroup }}</td>
            <td class="col-1 col-md-1 col-lg-1">{{ job.state }}</td>
            <td>{{ job.cronExpression }}</td>
            <td>{{ formatTime(job.nextFireTime) }}</td>
            <td>{{ formatTime(job.previousFireTime) }}</td>
            <td class="col-1 col-md-1 col-lg-1">{{ job.jobType }}</td>
            <td class="col-1 col-md-1 col-lg-1">{{ job.eventType }}</td>
            <td>{{ job.createdBy }}</td>
            <td>
              <button class="btn btn-sm btn-info me-1" @click="handleEdit(job)">수정</button>
              <button class="btn btn-sm btn-danger me-1" @click="handleDelete(job)">삭제</button>
              <button class="btn btn-sm btn-secondary" @click="handleToggleState(job)">
                {{ job.state === 'PAUSED' ? '시작' : '일시정지' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import axios from '@/api/axios'

interface JobInfo {
  jobName: string
  jobGroup: string
  state: string
  cronExpression: string
  nextFireTime: number
  previousFireTime: number
  jobType: string
  eventType: string
  createdBy: string
}

const jobs = ref<JobInfo[]>([])
const search = ref({
  jobName: '',
  jobGroup: '',
})

const formatTime = (time: number): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString()
}

const fetchJobs = async () => {
  try {
    const response = await axios.get('/api/quartz-jobs', {
      params: {
        jobName: search.value.jobName,
        jobGroup: search.value.jobGroup,
      },
    })
    jobs.value = response.data.data
  } catch (err) {
    console.error('잡 조회 실패', err)
  }
}

const handleEdit = (job: JobInfo) => {
  alert(`✏️ 수정 기능: ${job.jobName} / ${job.jobGroup}`)
  // router.push(`/jobs/${job.jobName}/edit`) 도 가능
}

const handleDelete = async (job: JobInfo) => {
  const confirmDelete = confirm(`${job.jobName} 잡을 삭제하시겠습니까?`)
  if (!confirmDelete) return
  try {
    await axios.delete('/api/quartz-jobs', {
      data: {
        jobName: job.jobName,
        jobGroup: job.jobGroup,
      },
    })
    await fetchJobs()
    alert('삭제 완료')
  } catch (err) {
    console.error('삭제 실패', err)
  }
}

const handleToggleState = async (job: JobInfo) => {
  const targetState = job.state === 'PAUSED' ? 'RESUME' : 'PAUSE'
  try {
    await axios.put(`/api/quartz-jobs/${targetState.toLowerCase()}`, {
      jobName: job.jobName,
      jobGroup: job.jobGroup,
    })
    await fetchJobs()
  } catch (err) {
    console.error('상태 변경 실패', err)
  }
}

onMounted(fetchJobs)
</script>

<style scoped></style>