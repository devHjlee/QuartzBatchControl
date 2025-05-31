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
                <th class="center-text">Batch Name</th>
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
            <h5 class="modal-title">{{ isEditMode ? 'Quartz Job 수정' : 'Quartz Job 등록' }}</h5>
            <button type="button" class="btn-close" @click="closeAddQuartzJobModal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
            <div class="mb-3">
              <label class="form-label">Job Type</label>
              <select class="form-control" v-model="newQuartzJobForm.jobType" :disabled="isEditMode">
                <option value="SIMPLE">SIMPLE</option>
                <option value="BATCH">BATCH</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Job Name</label>
              <input type="text" class="form-control" v-model="newQuartzJobForm.jobName" placeholder="Job Name 입력" :readonly="isEditMode">
            </div>

            <div class="mb-3" v-if="newQuartzJobForm.jobType === 'BATCH'">
              <label class="form-label">Batch Meta</label>
              <select class="form-control" v-model="newQuartzJobForm.selectedBatchMetaId" :disabled="isLoadingBatchMetas || isEditMode">
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
import { useRoute, useRouter } from 'vue-router'
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
  batchName: string;
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

// Modal state
const showAddQuartzJobModal = ref(false);
const isEditMode = ref(false); // 수정 모드 여부
const currentEditingJobId = ref<number | null>(null); // 수정 중인 Job의 ID

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

const route = useRoute(); // 현재 라우트 정보
const router = useRouter(); // 라우터 인스턴스

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

    // 라우트 쿼리에서 searchFromBatchTable 값을 읽어 searchInputValue에 설정
    if (route.query.searchFromBatchTable && typeof route.query.searchFromBatchTable === 'string') {
      searchInputValue.value = route.query.searchFromBatchTable;
      // 검색 후에는 URL에서 쿼리 파라미터를 제거하여 사용자가 직접 검색어를 변경하거나
      // 페이지를 새로고침했을 때 이전 검색어가 유지되지 않도록 함 (선택적)
      const currentPath = route.path;
      const currentQuery = { ...route.query };
      delete currentQuery.searchFromBatchTable;
      router.replace({ path: currentPath, query: currentQuery });
    }

    dataTable.value = (window as any).jQuery('#quartzMetaTable').DataTable({
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
      ajax: function (dtParams: any, callback: any, settings: any) {
        axios.get('/quartz-jobs', {
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            keyword: searchInputValue.value,
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
        { data: 'batchName' },
        { data: 'triggerState', className: 'text-center' },
        { data: 'cronExpression', className: 'text-center' },
        {
          data: 'prevFireTime',
          className: 'text-center',
          render: function(data: any) {
            return formatTime(data);
          }
        },
        {
          data: 'nextFireTime',
          className: 'text-center',
          render: function(data: any) {
            return formatTime(data);
          }
        },
        { data: 'createdBy', className: 'text-center' },
        {
          data: null,
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data: any, type: any, row: QuartzJobInfo) {
            let buttons = '';
            buttons += `<button class="btn btn-sm btn-info btn-circle action-trigger me-1" data-job-id="${row.id}" title="즉시 실행"><i class="fas fa-play"></i></button>`;
            buttons += `<button class="btn btn-sm btn-success btn-circle action-resume me-1" data-job-id="${row.id}" title="재개"><i class="fas fa-play-circle"></i></button>`;
            buttons += `<button class="btn btn-sm btn-warning btn-circle action-pause me-1" data-job-id="${row.id}" title="일시 정지"><i class="fas fa-pause-circle"></i></button>`;
            buttons += `<button class="btn btn-sm btn-danger btn-circle action-delete me-1" data-job-id="${row.id}" title="삭제"><i class="fas fa-trash"></i></button>`;
            buttons += `<button class="btn btn-sm btn-primary btn-circle action-edit" data-job-id="${row.id}" title="수정"><i class="fas fa-edit"></i></button>`;
            return buttons;
          }
        }
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customSearchContainer');
        if (customSearchContainer) {
          // Remove Bootstrap column classes if they exist on customSearchContainer itself
          customSearchContainer.classList.remove('col-sm-12', 'col-md-6');

          customSearchContainer.innerHTML = `
            <div class="input-group">
              <input type="text" class="form-control custom-search-input" placeholder="Job Name 검색">
              <button class="btn btn-outline-secondary custom-search-button mr-2" type="button">검색</button>
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

    $(document).off('click', '#quartzMetaTable button.action-trigger').on('click', '#quartzMetaTable button.action-trigger', async function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId && confirm('정말로 이 작업을 즉시 실행하시겠습니까?')) {
        await handleJobAction(`/quartz-jobs/trigger/${jobId}`, '즉시 실행');
      }
    });

    $(document).off('click', '#quartzMetaTable button.action-resume').on('click', '#quartzMetaTable button.action-resume', async function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId && confirm('정말로 이 작업을 재개하시겠습니까?')) {
        await handleJobAction(`/quartz-jobs/resume/${jobId}`, '재개');
      }
    });

    $(document).off('click', '#quartzMetaTable button.action-pause').on('click', '#quartzMetaTable button.action-pause', async function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId && confirm('정말로 이 작업을 일시 정지하시겠습니까?')) {
        await handleJobAction(`/quartz-jobs/pause/${jobId}`, '일시 정지');
      }
    });

    $(document).off('click', '#quartzMetaTable button.action-delete').on('click', '#quartzMetaTable button.action-delete', async function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId && confirm('정말로 이 작업을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        await handleJobAction(`/quartz-jobs/delete/${jobId}`, '삭제');
      }
    });
    
    $(document).off('click', '#quartzMetaTable button.action-edit').on('click', '#quartzMetaTable button.action-edit', function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      const jobToEdit = quartzJobs.value.find(job => job.id === jobId);
      if (jobToEdit) {
        openEditQuartzJobModal(jobToEdit);
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
      availableBatchMetas.value = response.data.data.map((item: any) => ({ id: item.id, metaName: item.metaName }));
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
  isEditMode.value = false; // 수정 모드 비활성화
  currentEditingJobId.value = null; // 수정 중인 Job ID 초기화
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

const openEditQuartzJobModal = (job: QuartzJobInfo) => {
  isEditMode.value = true;
  currentEditingJobId.value = job.id;
  newQuartzJobForm.value = {
    jobType: job.jobType as 'SIMPLE' | 'BATCH', // 타입 단언
    jobName: job.jobName,
    selectedBatchMetaId: job.jobType === 'BATCH' ? (job.batchMetaId || '') : '', // batchMetaId는 job 객체에 존재해야 함
    cronExpression: job.cronExpression || '' // job 객체에 cronExpression 존재해야 함
  };
  nextFireTimes.value = [];
  cronError.value = '';

  if (job.jobType === 'BATCH') {
    if (availableBatchMetas.value.length === 0 && !isLoadingBatchMetas.value) {
      fetchAvailableBatchMetas(); // BATCH 타입이면 메타 정보 로드 (이미 로드되었을 수 있음)
    }
  }
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
  if (!newQuartzJobForm.value.cronExpression.trim()) { // Cron Expression 유효성 검사 위치 변경
    alert('Cron Expression을 입력해주세요.');
    return;
  }

  const jobName = newQuartzJobForm.value.jobName.trim();
  const cronExpression = newQuartzJobForm.value.cronExpression.trim();
  let payload: any = {}; // payload 타입 명시를 위해 any 사용, 실제로는 더 구체적인 타입 권장

  if (isEditMode.value && currentEditingJobId.value !== null) {
    // 수정 모드
    const originalJob = quartzJobs.value.find(job => job.id === currentEditingJobId.value);
    if (!originalJob) {
        alert('수정할 작업을 찾을 수 없습니다.');
        return;
    }
    payload = {
        id: currentEditingJobId.value,
        jobType: newQuartzJobForm.value.jobType, // 수정 불가하지만, API 요구사항에 따라 전송
        jobName: newQuartzJobForm.value.jobName, // 수정 불가하지만, API 요구사항에 따라 전송
        jobGroup: newQuartzJobForm.value.jobType === 'SIMPLE' ? 'UTIL' : 'BATCH',
        cronExpression: cronExpression, // 이 값만 변경 가능
        misfirePolicy: 'FIRE_AND_PROCEED' // API 명세에 따라 고정값 또는 기존 값 사용
    };
    if (newQuartzJobForm.value.jobType === 'BATCH') {
        if (!newQuartzJobForm.value.selectedBatchMetaId) {
            alert('Batch Meta를 선택해주세요.');
            return;
        }
        payload.batchMetaId = newQuartzJobForm.value.selectedBatchMetaId;
    }

    try {
      // API 명세에 따라 POST 또는 PUT 사용. 여기서는 명세대로 POST
      const response = await axios.post('/quartz-jobs/update', payload); 
      if (response.data && response.data.success) {
        alert('Quartz Job이 성공적으로 수정되었습니다.');
        closeAddQuartzJobModal();
        if (dataTable.value) {
          dataTable.value.ajax.reload(null, false);
        }
      } else {
        alert(response.data?.message || 'Quartz Job 수정에 실패했습니다.');
      }
    } catch (error: any) {
      alert(error.response?.data?.message || 'Quartz Job 수정 중 오류가 발생했습니다.');
    }

  } else {
    // 추가 모드
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
        cronExpression: cronExpression,
        misfirePolicy: 'FIRE_AND_PROCEED',
        eventType: 'REGISTER'
      };
    } else { // SIMPLE type
      payload = {
        jobType: 'SIMPLE',
        jobName: jobName,
        jobGroup: 'UTIL',
        cronExpression: cronExpression,
        misfirePolicy: 'FIRE_AND_PROCEED',
        eventType: 'REGISTER'
      };
    }
    
    try {
      const response = await axios.post('/quartz-jobs', payload);
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
  }
};

// 공통 작업 처리 함수
const handleJobAction = async (url: string, actionName: string) => {
  try {
    const response = await axios.get(url); // 모든 액션이 GET 요청이라고 가정
    if (response.data && response.data.success) {
      alert(`작업 ${actionName} 요청이 성공적으로 처리되었습니다.`);
      if (dataTable.value) {
        dataTable.value.ajax.reload(null, false); // 현재 페이징 유지하며 리로드
      }
    } else {
      alert(response.data?.message || `작업 ${actionName} 요청에 실패했습니다.`);
    }
  } catch (error: any) {
    alert(error.response?.data?.message || `작업 ${actionName} 중 오류가 발생했습니다.`);
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

  // 라우트 쿼리가 변경될 때마다 테이블을 다시 로드 (searchFromBatchTable 쿼리가 있을 경우)
  watch(() => route.query.searchFromBatchTable, (newSearchTerm) => {
    if (newSearchTerm && typeof newSearchTerm === 'string') {
      searchInputValue.value = newSearchTerm;
      fetchQuartzJobs();
      // 검색 후에는 URL에서 쿼리 파라미터를 제거
      const currentPath = route.path;
      const currentQuery = { ...route.query };
      delete currentQuery.searchFromBatchTable;
      router.replace({ path: currentPath, query: currentQuery });
    } else if (!newSearchTerm && searchInputValue.value !== '') {
      // searchFromBatchTable 쿼리가 없어졌지만, 기존 검색어가 남아있는 경우 (예: 사용자가 직접 검색창을 지웠을 때)
      // 이 경우는 fetchQuartzJobs가 검색창의 searchInputValue.value를 사용하므로 별도 처리 불필요
      // 하지만, 만약 URL 쿼리 제거 후 즉시 테이블을 초기 상태로 되돌리고 싶다면 여기서 fetchQuartzJobs() 호출 가능
    }
  }, { immediate: true }); // 컴포넌트 마운트 시 즉시 실행
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
:deep(#customSearchContainer .input-group) {
  width: 100% !important;
  display: flex !important;
}

:deep(#customSearchContainer .custom-search-input) {
  flex-grow: 1 !important;
}

:deep(#customSearchContainer .custom-search-button),
:deep(#customSearchContainer .custom-add-button) {
  flex-grow: 0;
  flex-shrink: 0;
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
