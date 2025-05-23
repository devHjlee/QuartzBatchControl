<template>
  <div>
    <table id="jobTable" class="display" style="width: 100%">
      <thead>
      <tr>
        <th>Job Name</th>
        <th>Group</th>
        <th>Status</th>
        <th>Cron</th>
        <th>Next Run</th>
        <th>Last Run</th>
        <th>Type</th>
        <th>Event</th>
        <th>Created By</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <template v-if="jobs.length > 0">
        <tr v-for="job in jobs" :key="job.jobName + job.jobGroup">
          <td>{{ job.jobName }}</td>
          <td>{{ job.jobGroup }}</td>
          <td>{{ job.state }}</td>
          <td>{{ job.cronExpression }}</td>
          <td>{{ formatTime(job.nextFireTime) }}</td>
          <td>{{ formatTime(job.previousFireTime) }}</td>
          <td>{{ job.jobType }}</td>
          <td>{{ job.eventType }}</td>
          <td>{{ job.createdBy }}</td>
          <td>
            <button class="btn btn-sm btn-info me-1" @click="handleEdit(job)">수정</button>
            <button class="btn btn-sm btn-danger me-1" @click="handleDelete(job)">삭제</button>
            <button class="btn btn-sm btn-secondary" @click="handleToggleState(job)">
              {{ job.state === 'PAUSED' ? '시작' : '일시정지' }}
            </button>
          </td>
        </tr>
      </template>
      <template v-else>
        <tr>
          <td colspan="10">조회된 데이터가 없습니다.</td>
        </tr>
      </template>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
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
const dataTable = ref<any>(null)

const formatTime = (time: number): string => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const fetchJobs = async () => {
  try {
    const response = await axios.get('/quartz-jobs')
    jobs.value = response.data.data

    await nextTick()

    if (dataTable.value) {
      dataTable.value.clear().destroy()
    }

    dataTable.value = $('#jobTable').DataTable({
      paging: true,
      searching: true,
      info: true,
      language: {
        emptyTable: '조회된 데이터가 없습니다.',
        lengthMenu: '_MENU_개씩 보기',
        zeroRecords: '검색 결과가 없습니다.',
        info: '총 _TOTAL_건 중 _START_~_END_',
        paginate: {
          first: '처음',
          last: '마지막',
          next: '다음',
          previous: '이전',
        },
      },
    })
  } catch (err) {
    console.error('잡 조회 실패', err)
  }
}

const handleEdit = (job: JobInfo) => {
  alert(`수정: ${job.jobName} / ${job.jobGroup}`)
}

const handleDelete = async (job: JobInfo) => {
  if (!confirm(`정말로 삭제하시겠습니까? ${job.jobName}`)) return
  try {
    await axios.delete('/quartz-jobs', {
      data: { jobName: job.jobName, jobGroup: job.jobGroup },
    })
    await fetchJobs()
  } catch (err) {
    console.error('삭제 실패', err)
  }
}

const handleToggleState = async (job: JobInfo) => {
  const target = job.state === 'PAUSED' ? 'RESUME' : 'PAUSE'
  try {
    await axios.put(`/quartz-jobs/${target.toLowerCase()}`, {
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
