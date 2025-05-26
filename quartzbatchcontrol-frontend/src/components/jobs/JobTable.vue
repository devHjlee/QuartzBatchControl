<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">Quartz 관리</h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table id="quartzMetaTable" class="table table-bordered" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th class="center-text">Job Name</th>
                <th class="center-text">Job Type</th>
                <th class="center-text">Trigger State</th>
                <th class="center-text">Cron Expression</th>
                <th class="center-text">Prev Fire Time</th>
                <th class="center-text">Next Fire Time</th>
                <th class="center-text">Created By</th>
                <th class="center-text">Action</th>
              </tr>
            </thead>
            <tbody>
              <!-- DataTables will populate this section -->
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Add Quartz Job Modal -->
    <div v-if="showAddQuartzJobModal" class="modal fade show" style="display: block; background-color: rgba(0,0,0,0.5);" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Quartz Job 등록</h5>
            <button type="button" class="btn-close" @click="closeAddQuartzJobModal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
            <div class="mb-3">
              <label class="form-label">Job Type</label>
              <select class="form-control" v-model="newQuartzJobForm.jobType">
                <option value="SIMPLE">SIMPLE</option>
                <option value="BATCH">BATCH</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Job Name</label>
              <input type="text" class="form-control" v-model="newQuartzJobForm.jobName" placeholder="Job Name 입력">
            </div>

            <div class="mb-3" v-if="newQuartzJobForm.jobType === 'BATCH'">
              <label class="form-label">Batch Meta</label>
              <select class="form-control" v-model="newQuartzJobForm.selectedBatchMetaId" :disabled="isLoadingBatchMetas">
                <option value="" disabled>{{ isLoadingBatchMetas ? '로딩 중...' : '배치 메타를 선택하세요' }}</option>
                <option v-for="batchMeta in availableBatchMetas" :key="batchMeta.id" :value="batchMeta.id">{{ batchMeta.metaName }} (ID: {{ batchMeta.id }})</option>
              </select>
              <div v-if="isLoadingBatchMetas" class="spinner-border spinner-border-sm text-primary mt-2" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">Quartz Cron Expression</label>
              <div class="input-group">
                <input type="text" class="form-control" v-model="newQuartzJobForm.cronExpression" placeholder="e.g., 0 0/2 * * * ?">
                <button class="btn btn-outline-secondary" type="button" @click="fetchNextFireTimes" :disabled="isLoadingNextFireTimes">
                  <span v-if="isLoadingNextFireTimes" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                  {{ isLoadingNextFireTimes ? '확인 중...' : '다음 실행 시간 확인' }}
                </button>
              </div>
              <div v-if="nextFireTimes.length > 0 && !isLoadingNextFireTimes && !cronError" class="mt-2 mb-0">
                <p class="form-text mb-1"><strong>다음 실행 예정 시간:</strong></p>
                <ul class="list-group list-group-flush">
                  <li v-for="(time, index) in nextFireTimes" :key="index" class="list-group-item py-1 px-0 form-text">
                    {{ formatTime(time) }}
                  </li>
                </ul>
              </div>
              <div v-if="cronError && !isLoadingNextFireTimes" class="mt-2 text-danger form-text">
                {{ cronError }}
              </div>
            </div>

          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeAddQuartzJobModal">취소</button>
            <button type="button" class="btn btn-primary" @click="handleSaveNewQuartzJob">저장</button>
          </div>
        </div>
      </div>
    </div>

  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import axios from '@/api/axios'
import $ from 'jquery'

interface QuartzJobInfo {
  id: number;
  jobName: string;
  jobGroup: string;
  jobType: string;
  eventType: string;
  batchMetaId: number;
  triggerName: string;
  triggerGroup: string;
  nextFireTime: number;
  prevFireTime: number;
  triggerState: string;
  cronExpression: string;
  createdBy: string;
}

interface BatchMetaInfo {
  id: number;
  metaName: string;
  // 필요에 따라 다른 필드 추가 가능
}

interface NewQuartzJobForm {
  jobType: 'SIMPLE' | 'BATCH';
  jobName: string; // 사용자 입력 Job Name
  selectedBatchMetaId: number | string; // BATCH일 때 선택된 배치작업의 ID
  cronExpression: string;
}

const quartzJobs = ref<QuartzJobInfo[]>([])
const dataTable = ref<any>(null)
const searchInputValue = ref('');

// Add Quartz Job Modal state
const showAddQuartzJobModal = ref(false);
const newQuartzJobForm = ref<NewQuartzJobForm>({
  jobType: 'SIMPLE',
  jobName: '', // jobNameInput에서 jobName으로 변경
  selectedBatchMetaId: '',
  cronExpression: ''
});
const availableBatchMetas = ref<BatchMetaInfo[]>([]);
const isLoadingBatchMetas = ref(false);
const nextFireTimes = ref<number[]>([]);
const isLoadingNextFireTimes = ref(false);
const cronError = ref('');

watch(() => newQuartzJobForm.value.jobType, (newType) => {
  if (newType === 'SIMPLE') {
    newQuartzJobForm.value.selectedBatchMetaId = ''; // SIMPLE 타입으로 변경 시, 선택된 배치 메타 ID 초기화
  } else { // BATCH 타입
    // jobName은 이제 직접 입력이므로 BATCH 타입으로 변경 시 초기화하지 않음
    if (availableBatchMetas.value.length === 0 && !isLoadingBatchMetas.value) {
      fetchAvailableBatchMetas();
    }
  }
});

const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString()
}

const fetchQuartzJobs = async () => {
  try {
    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = window.jQuery('#quartzMetaTable').DataTable({
      paging: true,
      searching: false,
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      ordering: false, // 테이블 정렬 기능 비활성화
      pageLength: 10,
      lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "전체"]],
      dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"<"#customSearchContainer">>><"row"<"col-sm-12"tr>><"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
      language: {
        emptyTable: '조회된 데이터가 없습니다.',
        lengthMenu: '_MENU_개씩 보기',
        zeroRecords: '검색 결과가 없습니다.',
        info: '총 _TOTAL_건 중 _START_~_END_',
        infoEmpty: '데이터가 없습니다.',
        infoFiltered: '(전체 _MAX_건 중 검색결과)',
        paginate: {
          first: '처음',
          last: '마지막',
          next: '다음',
          previous: '이전',
        },
        processing: '데이터를 불러오는 중...',
      },
      ajax: function (dtParams, callback, settings) {
        axios.get('/quartz-jobs', {
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            jobName: searchInputValue.value,
          }
        })
        .then(function (response) {
          const backendResponse = response.data;
          if (backendResponse.success && backendResponse.data && backendResponse.data.content && backendResponse.data.page) {
            quartzJobs.value = backendResponse.data.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendResponse.data.page.totalElements,
              recordsFiltered: backendResponse.data.page.totalElements,
              data: backendResponse.data.content
            });
          } else {
            console.error("Invalid API response structure:", backendResponse);
            callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
          }
        })
        .catch(function (error) {
          console.error("API error:", error);
          callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
        });
      },
      columns: [
        { data: 'jobName' },
        { data: 'jobType', className: 'text-center' },
        { data: 'triggerState', className: 'text-center' },
        { data: 'cronExpression', className: 'text-center' },
        {
          data: 'prevFireTime',
          className: 'text-center',
          render: function(data) {
            return formatTime(data);
          }
        },
        {
          data: 'nextFireTime',
          className: 'text-center',
          render: function(data) {
            return formatTime(data);
          }
        },
        { data: 'createdBy', className: 'text-center' },
        {
          data: null,
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data, type, row) {
            return `
              <button class="btn btn-sm btn-info action-execute" data-job-id="${row.id}">실행</button>
            `;
          }
        }
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customSearchContainer');
        if (customSearchContainer) {
          customSearchContainer.innerHTML = `
            <div class="input-group">
              <input type="text" class="form-control custom-search-input" placeholder="Job Name 검색">
              <button class="btn btn-outline-secondary custom-search-button me-2" type="button">검색</button>
              <button class="btn btn-primary custom-add-button" type="button">추가</button>
            </div>
          `;

          const searchInput = customSearchContainer.querySelector('.custom-search-input');
          const searchButton = customSearchContainer.querySelector('.custom-search-button');
          const addButton = customSearchContainer.querySelector('.custom-add-button');

          if (searchInput instanceof HTMLInputElement && searchButton && addButton) {
            searchInput.addEventListener('keyup', (event) => {
              searchInputValue.value = searchInput.value;
              if (event.key === 'Enter') {
                dataTable.value.ajax.reload();
              }
            });
            searchButton.addEventListener('click', () => {
              searchInputValue.value = searchInput.value;
              dataTable.value.ajax.reload();
            });
            addButton.addEventListener('click', () => {
              openAddQuartzJobModal();
            });
          }
        }
      }
    });

    $(document).off('click', '#quartzMetaTable button.action-execute').on('click', '#quartzMetaTable button.action-execute', function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId) {
        if (confirm('정말로 이 작업을 실행하시겠습니까?')) {
          handleExecuteJob(jobId);
        }
      }
    });

  } catch (err) {
    console.error('Quartz 작업 조회 또는 DataTable 초기화 실패', err)
  }
}

const fetchAvailableBatchMetas = async () => {
  isLoadingBatchMetas.value = true;
  try {
    const response = await axios.get('/batch/all'); // API 엔드포인트 확인 필요
    if (response.data && response.data.success && Array.isArray(response.data.data)) {
      availableBatchMetas.value = response.data.data.map(item => ({ id: item.id, metaName: item.metaName }));
      // BATCH 타입이 기본 선택이고, 데이터 로드 후 첫번째 항목 자동 선택 (선택사항)
      // if (newQuartzJobForm.value.jobType === 'BATCH' && availableBatchMetas.value.length > 0 && !newQuartzJobForm.value.selectedBatchMetaId) {
      //   newQuartzJobForm.value.selectedBatchMetaId = availableBatchMetas.value[0].id;
      // }
    } else {
      console.error('Failed to load available batch metas or malformed response:', response.data);
      alert('사용 가능한 배치 작업 목록을 불러오는데 실패했습니다.');
      availableBatchMetas.value = []; // 실패 시 빈 배열로 초기화
    }
  } catch (error) {
    console.error('Error fetching available batch metas:', error);
    alert('사용 가능한 배치 작업 목록 조회 중 오류가 발생했습니다.');
    availableBatchMetas.value = []; // 에러 시 빈 배열로 초기화
  } finally {
    isLoadingBatchMetas.value = false;
  }
};

const openAddQuartzJobModal = () => {
  newQuartzJobForm.value = {
    jobType: 'SIMPLE',
    jobName: '', // jobNameInput에서 jobName으로 변경
    selectedBatchMetaId: '',
    cronExpression: ''
  };
  availableBatchMetas.value = []; // 모달 열 때마다 이전 목록 초기화
  nextFireTimes.value = []; // 모달 열 때 다음 실행 시간 초기화
  cronError.value = '';      // 모달 열 때 에러 메시지 초기화
  // SIMPLE 타입이 기본이므로, BATCH 메타 정보는 BATCH 타입 선택 시 로드 (watch 로직에서 처리)
  showAddQuartzJobModal.value = true;
};

const closeAddQuartzJobModal = () => {
  showAddQuartzJobModal.value = false;
};

const handleSaveNewQuartzJob = async () => {
  if (!newQuartzJobForm.value.jobName.trim()) {
    alert('Job Name을 입력해주세요.');
    return;
  }

  const jobName = newQuartzJobForm.value.jobName.trim();
  let payload = {};

  if (newQuartzJobForm.value.jobType === 'BATCH') {
    if (!newQuartzJobForm.value.selectedBatchMetaId) {
      alert('Batch Meta를 선택해주세요.');
      return;
    }
    payload = {
      jobType: 'BATCH',
      jobName: jobName,
      jobGroup: 'BATCH',
      batchMetaId: newQuartzJobForm.value.selectedBatchMetaId,
      cronExpression: newQuartzJobForm.value.cronExpression.trim(),
      misfirePolicy: 'FIRE_AND_PROCEED',
      eventType: 'REGISTER'
    };
  } else { // SIMPLE type
    payload = {
      jobType: 'SIMPLE',
      jobName: jobName,
      jobGroup: 'UTIL',
      cronExpression: newQuartzJobForm.value.cronExpression.trim(),
      misfirePolicy: 'FIRE_AND_PROCEED',
      eventType: 'REGISTER'
    };
  }

  if (!newQuartzJobForm.value.cronExpression.trim()) {
    alert('Cron Expression을 입력해주세요.');
    return;
  }

  try {
    const response = await axios.post('/quartz-jobs', payload); // API 엔드포인트 확인 필요
    if (response.data && response.data.success) {
      alert('Quartz Job이 성공적으로 등록되었습니다.');
      closeAddQuartzJobModal();
      if (dataTable.value) {
        dataTable.value.ajax.reload(null, false);
      }
    } else {
      alert(response.data?.message || 'Quartz Job 등록에 실패했습니다.');
    }
  } catch (error: any) {
    alert(error.response?.data?.message || 'Quartz Job 등록 중 오류가 발생했습니다.');
  }
};

const handleExecuteJob = async (jobId: number) => {
  try {
    const response = await axios.post(`/quartz-jobs/execute/${jobId}`); // 엔드포인트는 /quartz-jobs 기준으로 변경될 수 있음
    if (response.data && response.data.success) {
      alert('작업이 성공적으로 실행 요청되었습니다.');
      if (dataTable.value) {
        dataTable.value.ajax.reload(null, false);
      }
    } else {
      alert(response.data?.message || '작업 실행 요청에 실패했습니다.');
    }
  } catch (error: any) {
    alert(error.response?.data?.message || '작업 실행 중 오류가 발생했습니다.');
  }
};

const fetchNextFireTimes = async () => {
  if (!newQuartzJobForm.value.cronExpression.trim()) {
    cronError.value = 'Cron 표현식을 입력해주세요.';
    nextFireTimes.value = [];
    return;
  }
  isLoadingNextFireTimes.value = true;
  nextFireTimes.value = [];
  cronError.value = '';
  try {
    const response = await axios.get('/quartz-jobs/preview-schedule', {
      params: {
        cronExpression: newQuartzJobForm.value.cronExpression.trim(),
        numTimes: 5 // 5개의 다음 실행 시간을 가져옴
      }
    });
    if (response.data && response.data.success && Array.isArray(response.data.data)) {
      if (response.data.data.length === 0) {
        cronError.value = '입력된 Cron 표현식에 대한 다음 실행 시간을 찾을 수 없습니다.';
      } else {
        nextFireTimes.value = response.data.data;
      }
    } else {
      cronError.value = response.data?.message || 'Cron 표현식을 해석할 수 없거나 유효하지 않습니다.';
    }
  } catch (error: any) {
    console.error("Error fetching next fire times:", error);
    cronError.value = error.response?.data?.message || '다음 실행 시간 조회 중 오류가 발생했습니다.';
  } finally {
    isLoadingNextFireTimes.value = false;
  }
};

onMounted(() => {
  fetchQuartzJobs();
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

.me-2 {
  margin-right: 0.5rem !important; /* Ensure spacing for the add button */
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

/* DataTables 검색 필터 스타일 */
.dataTables_filter {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.dataTables_filter input {
  margin-right: 0.5rem;
}

/* Custom search styles */
#customSearchContainer .input-group {
  /*justify-content: flex-end; */
}

#customSearchContainer .custom-search-input {
  /* flex-grow: 1; */
}

#customSearchContainer .custom-search-button {
  /* margin-left: 0.5rem; */
}

.modal-body {
  max-height: 70vh;
  overflow-y: auto;
}

.btn-close {
  padding: 1rem;
  margin: -1rem -1rem -1rem auto;
}

.btn-close span {
  font-size: 1.5rem;
  line-height: 1;
  color: #000;
  text-shadow: 0 1px 0 #fff;
  opacity: .5;
}

.btn-close:hover span {
  opacity: .75;
}

</style>
