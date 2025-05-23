<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">배치 작업 목록</h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table id="batchMetaTable" class="table table-bordered" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th>Batch Job</th>
                <th>Meta Name</th>
                <th>Description</th>
                <th>Quartz 연동</th>
                <th>Created By</th>
                <th>Created At</th>
<!--                <th>Actions</th>-->
              </tr>
            </thead>
            <tbody>
              <template v-if="batchJobs.length > 0">
                <tr v-for="job in batchJobs" :key="job.id">
                  <td>{{ job.jobName }}</td>
                  <td>{{ job.metaName }}</td>
                  <td>{{ job.jobDescription }}</td>
                  <td>{{ job.registeredInQuartz }}</td>
                  <td>{{ job.createdBy }}</td>
                  <td>{{ formatTime(job.createdAt) }}</td>
<!--                  <td>-->
<!--                    <button class="btn btn-sm btn-info me-1" @click="handleEdit(job)">수정</button>-->
<!--                    <button class="btn btn-sm btn-danger me-1" @click="handleDelete(job)">삭제</button>-->
<!--                    <button class="btn btn-sm btn-primary" @click="handleExecute(job)">실행</button>-->
<!--                  </td>-->
                </tr>
              </template>
              <template v-else>
                <tr>
                  <td colspan="6">조회된 데이터가 없습니다.</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
  <!-- /.container-fluid -->

</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import axios from '@/api/axios'

interface BatchJobMeta {
  id: number
  jobName: string
  metaName: string
  jobDescription: string
  registeredInQuartz: boolean
  createdBy: string
  createdAt: string
}

interface PageResponse {
  content: BatchJobMeta[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

const batchJobs = ref<BatchJobMeta[]>([])
const dataTable = ref<any>(null)
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

const formatTime = (time: string): string => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const fetchBatchJobs = async (page = 0, size = 10, search = '') => {
  try {
    const response = await axios.get('/batch', {
      params: {
        page,
        size,
        search
      }
    })
    const data = response.data.data
    batchJobs.value = data.content
    totalElements.value = data.totalElements

    await nextTick()

    if (dataTable.value) {
      dataTable.value.clear().destroy()
    }

    dataTable.value = $('#batchMetaTable').DataTable({
      paging: true,
      searching: true,
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      pageLength: size,
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
      dom: '<"top"lf>rt<"bottom"ip><"clear">',
      language: {
        emptyTable: '조회된 데이터가 없습니다.',
        lengthMenu: '_MENU_개씩 보기',
        zeroRecords: '검색 결과가 없습니다.',
        info: '총 _TOTAL_건 중 _START_~_END_',
        infoEmpty: '데이터가 없습니다.',
        infoFiltered: '(전체 _MAX_건 중 검색결과)',
        search: '검색:',
        paginate: {
          first: '처음',
          last: '마지막',
          next: '다음',
          previous: '이전',
        },
        processing: '데이터를 불러오는 중...',
      },
      ajax: function (dtParams, callback, settings) {
        axios.get('/batch', {
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            jobName: dtParams.search.value,
            metaName: dtParams.search.value
          }
        })
        .then(function (response) {
          const backendPayload = response.data.data;

          if (backendPayload && backendPayload.page && Array.isArray(backendPayload.content)) {
            callback({
              draw: dtParams.draw,
              recordsTotal: backendPayload.page.totalElements,
              recordsFiltered: backendPayload.page.totalElements,
              data: backendPayload.content
            });
          } else {
            console.error("DataTables: 백엔드로부터 잘못된 데이터 구조 수신:", response.data);
            callback({
              draw: dtParams.draw,
              recordsTotal: 0,
              recordsFiltered: 0,
              data: []
            });
          }
        })
        .catch(function (error) {
          console.error("DataTables: 데이터 조회 중 오류 발생:", error);
          callback({
            draw: dtParams.draw,
            recordsTotal: 0,
            recordsFiltered: 0,
            data: []
          });
        });
      },
      columns: [
        { data: 'jobName' },
        { data: 'metaName' },
        { data: 'jobDescription', defaultContent: '-' },
        { data: 'registeredInQuartz'},
        { data: 'createdBy' },
        {
          data: 'createdAt',
          render: function(data, type, row) {
            return formatTime(data);
          }
        }
      ]
    })
  } catch (err) {
    console.error('배치 작업 메타 조회 실패', err)
  }
}

const handleEdit = (job: BatchJobMeta) => {
  // TODO: 수정 모달 또는 페이지로 이동
  console.log('수정:', job)
}

const handleDelete = async (job: BatchJobMeta) => {
  if (!confirm(`정말로 삭제하시겠습니까? ${job.metaName}`)) return
  try {
    await axios.delete(`/batch/${job.id}`)
    await fetchBatchJobs()
  } catch (err) {
    console.error('삭제 실패', err)
  }
}

const handleExecute = async (job: BatchJobMeta) => {
  try {
    await axios.post('/batch/execute', { id: job.id })
    alert('배치 작업 실행이 요청되었습니다.')
  } catch (err) {
    console.error('실행 실패', err)
  }
}

onMounted(fetchBatchJobs)
</script>

<style scoped>
.table-responsive {
  overflow-x: auto;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5;
  border-radius: 0.2rem;
}

.me-1 {
  margin-right: 0.25rem;
}

.card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  word-wrap: break-word;
  background-color: #fff;
  background-clip: border-box;
  border: 1px solid #e3e6f0;
  border-radius: 0.35rem;
}

.card-header {
  padding: 0.75rem 1.25rem;
  margin-bottom: 0;
  background-color: #f8f9fc;
  border-bottom: 1px solid #e3e6f0;
}

.card-body {
  flex: 1 1 auto;
  min-height: 1px;
  padding: 1.25rem;
}

.shadow {
  box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15) !important;
}

.mb-4 {
  margin-bottom: 1.5rem !important;
}

.font-weight-bold {
  font-weight: 700 !important;
}

.text-primary {
  color: #4e73df !important;
}
</style>
