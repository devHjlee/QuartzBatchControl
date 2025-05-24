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
                <th class="center-text">Batch Job</th>
                <th class="center-text">Meta Name</th>
                <th class="center-text">Description</th>
                <th class="center-text">Parameters</th>
                <th class="center-text">Quartz 연동</th>
                <th class="center-text">Created By</th>
                <!-- <th>Actions</th> -->
              </tr>
            </thead>
            <tbody>
              <template v-if="batchJobs.length > 0">
                <tr v-for="job in batchJobs" :key="job.id">
                  <td>{{ job.jobName }}</td>
                  <td>{{ job.metaName }}</td>
                  <td>{{ job.jobDescription }}</td>
                  <td>{{ job.jobParameters }}</td>
                  <td >{{ job.registeredInQuartz }}</td>
                  <td>{{ job.createdBy }}</td>
                  <!--<td>-->
                  <!--  <button class="btn btn-sm btn-info me-1" @click="handleEdit(job)">수정</button>-->
                  <!--  <button class="btn btn-sm btn-danger me-1" @click="handleDelete(job)">삭제</button>-->
                  <!--  <button class="btn btn-sm btn-primary" @click="handleExecute(job)">실행</button>-->
                  <!--</td>-->
                </tr>
              </template>
              <template v-else>
                <tr>
                  <td colspan="5">조회된 데이터가 없습니다.</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Parameters Modal -->
    <div v-if="showParametersModal" class="modal fade show" style="display: block; background-color: rgba(0,0,0,0.5);" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Job Parameters</h5>
            <button type="button" class="btn-close" @click="closeParametersModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="isLoadingParameters" class="text-center">
              <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>
            <div v-else-if="currentJobParameters">
              <pre>{{ JSON.stringify(currentJobParameters, null, 2) }}</pre>
            </div>
            <div v-else>
              <p>파라미터 정보를 불러오지 못했습니다.</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeParametersModal">Close</button>
          </div>
        </div>
      </div>
    </div>

  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import axios from '@/api/axios'
import $ from 'jquery'

interface BatchJobMeta {
  id: number
  jobName: string
  metaName: string
  jobDescription: string
  jobParameters: boolean
  registeredInQuartz: boolean
  createdBy: string
  // createdAt: string // Removed as per request
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

// Modal state
const showParametersModal = ref(false)
const currentJobParameters = ref<any | null>(null)
const isLoadingParameters = ref(false)

const formatTime = (time: string): string => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const fetchBatchJobs = async (page = 0, size = 10, search = '') => {
  try {
    // This initial fetch is now primarily handled by DataTables server-side processing
    // We might keep it for initial non-DataTables display or remove if DataTables handles all
    // For now, DataTables ajax will be the main driver.

    await nextTick()

    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = window.jQuery('#batchMetaTable').DataTable({
      paging: true,
      searching: true,
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      pageLength: size,
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
      dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>rt<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
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
            jobName: dtParams.search.value, // 백엔드 검색 파라미터
            metaName: dtParams.search.value, // 백엔드 검색 파라미터
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
        {
          data: 'jobParameters',
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data, type, row) {
            if (data === true) {
              return `<a href="#" class="btn btn-info btn-circle btn-sm action-view-parameters" data-job-id="${row.id}" title="View Parameters"><i class="fas fa-search fa-sm"></i></a>`;
            }
            return ''; // False면 빈 칸
          }
        },
        {
          data: 'registeredInQuartz',
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data, type, row) {
            if (data === true) {
              return `<a href="#" class="btn btn-success btn-circle btn-sm action-view-job" data-job-id="${row.id}" title="Job 상세 보기"><i class="fas fa-check"></i></a>`;
            }
            return ''; // False면 빈 칸
          }
        },
        { data: 'createdBy' }
        // { data: 'createdAt' } // Removed column
      ]
    });

    // Event delegation for dynamically created buttons (if needed in future)
    // $('#batchMetaTable tbody').off('click', 'a.action-view-job').on('click', 'a.action-view-job', function (e) {
    //   e.preventDefault();
    //   const jobId = $(this).data('job-id');
    //   console.log('View job details for ID:', jobId);
    //   // 여기서 Vue 라우터나 메소드를 호출하여 job 상세 페이지로 이동할 수 있습니다.
    //   // 예: router.push({ name: 'JobDetails', params: { id: jobId } });
    // });

  } catch (err) {
    console.error('배치 작업 메타 조회 또는 DataTable 초기화 실패', err)
  }
}

// handleEdit, handleDelete, handleExecute는 현재 DataTables render에서 직접 호출되지 않으므로,
// 필요하다면 위와 같은 이벤트 위임 방식으로 수정하거나, Vue 컴포넌트 내 다른 방식으로 연동해야 합니다.
const handleEdit = (job: BatchJobMeta) => {
  console.log('수정 시도 (이벤트 위임 필요):', job)
}

const handleDelete = async (job: BatchJobMeta) => {
  console.log('삭제 시도 (이벤트 위임 필요):', job)
}

const handleExecute = async (job: BatchJobMeta) => {
  console.log('실행 시도 (이벤트 위임 필요):', job)
}

const openParametersModal = async (jobId: number) => {
  showParametersModal.value = true
  isLoadingParameters.value = true
  currentJobParameters.value = null
  try {
    // Consistent with prior API calls, using /batch/{id}
    // The controller is at /api/batch, getJobParameters is at /{metaId}
    // So if /batch is mapped to /api/batch by proxy/axios config, this is /api/batch/{jobId}
    const response = await axios.get(`/batch/${jobId}`)
    if (response.data && response.data.success) {
      currentJobParameters.value = response.data.data // Assuming the actual params are in response.data.data
    } else {
      console.error("Failed to load job parameters:", response.data.message)
      currentJobParameters.value = { error: response.data.message || "Unknown error" }
    }
  } catch (error) {
    console.error("Error fetching job parameters:", error)
    currentJobParameters.value = { error: "Failed to fetch parameters due to a network or server error." }
  }
  isLoadingParameters.value = false
}

const closeParametersModal = () => {
  showParametersModal.value = false
  currentJobParameters.value = null
}

onMounted(() => {
  fetchBatchJobs(currentPage.value, pageSize.value);
  // 이벤트 위임 추가 (동적으로 생성된 버튼 대응)
  $(document).on('click', 'a.action-view-parameters', function (e) {
    e.preventDefault();
    const jobId = $(this).data('job-id');
    if (jobId) {
      openParametersModal(jobId);
    }
  });
});
</script>

<style scoped>
.center-text {
  text-align: center;
}

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

/* Card and other styles from SB Admin 2 theme if not globally applied */
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

/* Ensure DataTables controls are styled correctly with Bootstrap */
:deep(.dataTables_wrapper .row:first-child) {
  margin-bottom: 0.5rem; /* Adjust spacing if needed */
}

:deep(.dataTables_length label),
:deep(.dataTables_filter label) {
  margin-bottom: 0; /* Align items vertically if they wrap */
}

.modal.fade.show {
  display: block;
  background-color: rgba(0,0,0,0.5);
}
.modal-dialog {
  margin-top: 5rem; /* Adjust as needed */
}

.btn-circle.btn-sm {
    width: 2rem;
    height: 2rem;
    padding: 0.5rem 0;
    border-radius: 1rem;
    font-size: .75rem;
    line-height: 1.4;
}
.btn-info.btn-circle {
    background-color: #17a2b8; /* Bootstrap info color */
    border-color: #17a2b8;
    color: white;
}
.btn-info.btn-circle:hover {
    background-color: #138496;
    border-color: #117a8b;
}
</style>
