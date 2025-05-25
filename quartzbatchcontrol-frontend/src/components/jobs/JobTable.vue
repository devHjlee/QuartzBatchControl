<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">Batch 관리</h6>
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

    <!-- Batch Job Modal -->
    <div v-if="showParametersModal" class="modal fade show" style="display: block; background-color: rgba(0,0,0,0.5);" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ currentJobIdForModal ? '배치 작업 수정' : '배치 작업 등록' }}</h5>
            <button type="button" class="btn-close" @click="closeParametersModal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
            <div class="mb-3">
              <label class="form-label">Batch Job</label>
              <template v-if="!currentJobIdForModal"> 
                <select class="form-control" v-model="batchJobForm.jobName" :disabled="isLoadingAvailableJobs">
                  <option value="" disabled>{{ isLoadingAvailableJobs ? '로딩 중...' : '배치 작업을 선택하세요' }}</option>
                  <option v-for="jobName in availableBatchJobs" :key="jobName" :value="jobName">{{ jobName }}</option>
                </select>
                <div v-if="isLoadingAvailableJobs" class="spinner-border spinner-border-sm text-primary mt-2" role="status">
                  <span class="visually-hidden">Loading...</span>
                </div>
              </template>
              <template v-else>
                <input type="text" class="form-control" v-model="batchJobForm.jobName" placeholder="Batch Job 입력" readonly>
              </template>
            </div>
            <div class="mb-3">
              <label class="form-label">Meta Name</label>
              <input type="text" class="form-control" v-model="batchJobForm.metaName" placeholder="Meta Name 입력">
            </div>
            <div class="mb-3">
              <label class="form-label">Description</label>
              <textarea class="form-control" v-model="batchJobForm.jobDescription" placeholder="Description 입력"></textarea>
            </div>
            
            <div class="mb-3">
              <button class="btn btn-primary" @click="handleAddParameter">파라미터 추가</button>
            </div>

            <table class="table table-bordered">
              <thead>
                <tr>
                  <th>Key</th>
                  <th>Type</th>
                  <th>Value</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isNewParameter">
                  <td>
                    <input type="text" class="form-control" v-model="newParameterKey" placeholder="키 입력" />
                  </td>
                  <td>
                    <select class="form-control" v-model="newParameterType">
                      <option value="String">String</option>
                      <option value="Number">Number</option>
                      <option value="Boolean">Boolean</option>
                    </select>
                  </td>
                  <td>
                    <template v-if="newParameterType === 'Boolean'">
                      <select class="form-control" v-model="newParameterValue">
                        <option value="true">true</option>
                        <option value="false">false</option>
                      </select>
                    </template>
                    <input 
                      v-else
                      type="text" 
                      class="form-control" 
                      v-model="newParameterValue" 
                      placeholder="값 입력"
                    />
                  </td>
                  <td>
                    <button class="btn btn-sm btn-success me-1" @click="confirmAddParameter">확인</button>
                    <button class="btn btn-sm btn-secondary" @click="cancelAddParameter">취소</button>
                  </td>
                </tr>
                <tr v-for="(value, key) in parsedParameters" :key="key">
                  <td>{{ key }}</td>
                  <td>
                    <select class="form-control" v-model="parameterTypes[key]" @change="updateParameterType(key)">
                      <option value="String">String</option>
                      <option value="Number">Number</option>
                      <option value="Boolean">Boolean</option>
                    </select>
                  </td>
                  <td>
                    <template v-if="parameterTypes[key] === 'Boolean'">
                      <select class="form-control" :value="value" @change="updateParameterValue(key, $event)">
                        <option value="true">true</option>
                        <option value="false">false</option>
                      </select>
                    </template>
                    <input 
                      v-else
                      type="text" 
                      class="form-control" 
                      :value="value" 
                      @input="updateParameterValue(key, $event)"
                    />
                  </td>
                  <td>
                    <button class="btn btn-sm btn-danger" @click="handleDeleteParameter(key)">삭제</button>
                  </td>
                </tr>
                <tr v-if="!parsedParameters || Object.keys(parsedParameters).length === 0">
                  <td colspan="4" class="text-center">파라미터가 없습니다.</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeParametersModal">취소</button>
            <button type="button" class="btn btn-primary" @click="handleSaveBatchJob">저장</button>
          </div>
        </div>
      </div>
    </div>

  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick, computed } from 'vue'
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

interface BatchJobForm {
  jobName: string
  metaName: string
  jobDescription: string
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
const currentJobIdForModal = ref<number | null>(null)
const isNewParameter = ref(false)
const newParameterKey = ref('')
const newParameterValue = ref('')
const newParameterType = ref('String')
const parameterTypes = ref<Record<string, string>>({})
const parsedParameters = ref<Record<string, any>>({})
const batchJobForm = ref<BatchJobForm>({
  jobName: '',
  metaName: '',
  jobDescription: ''
})

const searchInputValue = ref(''); // To store search input

const availableBatchJobs = ref<string[]>([])
const isLoadingAvailableJobs = ref(false)

const getTypeFromValue = (value: any): string => {
  if (typeof value === 'boolean') return 'Boolean';
  if (typeof value === 'number') return 'Number';
  return 'String';
}

const convertValueByType = (value: any, type: string): any => {
  switch (type) {
    case 'Number':
      return Number(value);
    case 'Boolean':
      return value === 'true';
    default:
      return String(value);
  }
}

const formatTime = (time: string): string => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const fetchBatchJobs = async (page = 0, size = 10) => {
  try {
    await nextTick()

    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = window.jQuery('#batchMetaTable').DataTable({
      paging: true,
      searching: false, // Disable DataTables default search, we'll use a custom one
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      pageLength: size,
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
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
        axios.get('/batch', {
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            jobName: searchInputValue.value, // Use our reactive search input value
            metaName: searchInputValue.value, // Use our reactive search input value
          }
        })
        .then(function (response) {
          const backendPayload = response.data.data;
          if (backendPayload && backendPayload.page && Array.isArray(backendPayload.content)) {
            batchJobs.value = backendPayload.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendPayload.page.totalElements,
              recordsFiltered: backendPayload.page.totalElements,
              data: backendPayload.content
            });
          } else {
            callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
          }
        })
        .catch(function () {
          callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
        });
      },
      columns: [
        { data: 'jobName' },
        { data: 'metaName' },
        { data: 'jobDescription', defaultContent: '-' },
        { data: 'jobParameterSize', className: 'text-center' },
        {
          data: 'registeredInQuartz',
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data, type, row) {
            if (data === true) {
              return `<a href="#" class="btn btn-success btn-circle btn-sm action-view-job" data-job-id="${row.id}" title="Job 상세 보기"><i class="fas fa-check"></i></a>`;
            }
            return '';
          }
        },
        { data: 'createdBy' },
        {
          data: null,
          orderable: false,
          searchable: false,
          className: 'text-center',
          render: function(data, type, row) {
            return `
              <button class="btn btn-sm btn-primary action-edit me-1" data-job-id="${row.id}">수정</button>
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
              <input type="text" class="form-control custom-search-input" placeholder="검색">
              <button class="btn btn-outline-secondary custom-search-button" type="button">검색</button>
              <button class="btn btn-primary ms-2 custom-add-button" type="button">추가</button>
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
              searchInputValue.value = searchInput.value; // Ensure value is updated before reload
              dataTable.value.ajax.reload();
            });
            addButton.addEventListener('click', () => {
              handleAddNew();
            });
          }
        }
      }
    });

    // 수정 버튼 이벤트 바인딩 (중복 방지)
    $(document).off('click', '#batchMetaTable button.action-edit').on('click', '#batchMetaTable button.action-edit', function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      const job = batchJobs.value.find(j => j.id === jobId);
      if (job) {
        handleEdit(job);
      }
    });

    // 실행 버튼 이벤트 바인딩
    $(document).off('click', '#batchMetaTable button.action-execute').on('click', '#batchMetaTable button.action-execute', function (e) {
      e.preventDefault();
      const jobId = $(this).data('job-id');
      if (jobId) {
        if (confirm('정말로 이 작업을 실행하시겠습니까?')) {
          handleExecuteJob(jobId);
        }
      }
    });

  } catch (err) {
    console.error('배치 작업 메타 조회 또는 DataTable 초기화 실패', err)
  }
}

const handleEdit = async (job: BatchJobMeta) => {
  currentJobIdForModal.value = job.id;
  showParametersModal.value = true;
  isLoadingParameters.value = true;

  batchJobForm.value = { jobName: '', metaName: '', jobDescription: '' };
  parsedParameters.value = {};
  parameterTypes.value = {};
  isNewParameter.value = false;
  availableBatchJobs.value = [];

  try {
    const response = await axios.get(`/batch/${job.id}`);
    if (response.data && response.data.success) {
      const jobDetails = response.data.data;
      batchJobForm.value = {
        jobName: jobDetails.jobName,
        metaName: jobDetails.metaName,
        jobDescription: jobDetails.jobDescription
      };

      if (jobDetails.jobParameters) {
        try {
          const params = typeof jobDetails.jobParameters === 'string' 
            ? JSON.parse(jobDetails.jobParameters)
            : jobDetails.jobParameters;
          
          parsedParameters.value = params;
          Object.entries(params).forEach(([key, value]) => {
            parameterTypes.value[key] = getTypeFromValue(value);
          });
        } catch (e) {
          alert('파라미터 정보를 파싱하는데 실패했습니다.');
        }
      }
    } else {
      alert(response.data?.message || '배치 상세 정보를 불러오는데 실패했습니다.');
      closeParametersModal();
    }
  } catch (error) {
    alert('배치 상세 정보 조회 중 오류가 발생했습니다.');
    closeParametersModal();
  } finally {
    isLoadingParameters.value = false;
  }
}

const handleAddNew = async () => {
  currentJobIdForModal.value = null;
  batchJobForm.value = { jobName: '', metaName: '', jobDescription: '' };
  parsedParameters.value = {};
  parameterTypes.value = {};
  isNewParameter.value = false;

  isLoadingAvailableJobs.value = true;
  availableBatchJobs.value = [];

  try {
    const response = await axios.get('/batch/available');
    if (response.data && response.data.success && Array.isArray(response.data.data)) {
      availableBatchJobs.value = response.data.data;
      if (availableBatchJobs.value.length > 0) {
        // batchJobForm.value.jobName = availableBatchJobs.value[0];
      }
    } else {
      console.error('Failed to load available batch jobs or malformed response:', response.data);
      alert('사용 가능한 배치 작업 목록을 불러오는데 실패했습니다.');
    }
  } catch (error) {
    console.error('Error fetching available batch jobs:', error);
    alert('사용 가능한 배치 작업 목록 조회 중 오류가 발생했습니다.');
  } finally {
    isLoadingAvailableJobs.value = false;
  }

  showParametersModal.value = true;
}

const handleSaveBatchJob = async () => {
  try {
    const data = {
      id: currentJobIdForModal.value,
      ...batchJobForm.value,
      jobParameters: parsedParameters.value
    };

    if (currentJobIdForModal.value) {
      await axios.put(`/batch`, data);
    } else {
      await axios.post('/batch', data);
    }

    alert('저장되었습니다.');
    closeParametersModal();
    fetchBatchJobs();
  } catch (error) {
    alert(error.response?.data?.message || '작업 실행 중 오류가 발생했습니다.');
  }
}

const closeParametersModal = () => {
  showParametersModal.value = false;
  currentJobParameters.value = null;
  currentJobIdForModal.value = null;
  parsedParameters.value = {};
  parameterTypes.value = {};
}

const handleAddParameter = () => {
  isNewParameter.value = true;
  newParameterKey.value = '';
  newParameterValue.value = '';
  newParameterType.value = 'String';
}

const confirmAddParameter = () => {
  if (!newParameterKey.value.trim()) {
    alert('키를 입력해주세요.');
    return;
  }
  
  if (parsedParameters.value && newParameterKey.value in parsedParameters.value) {
    alert('이미 존재하는 키입니다.');
    return;
  }

  if (newParameterType.value === 'Number' && isNaN(Number(newParameterValue.value))) {
    alert('숫자만 입력 가능합니다.');
    return;
  }

  if (parsedParameters.value) {
    const convertedValue = convertValueByType(newParameterValue.value, newParameterType.value);
    parsedParameters.value[newParameterKey.value] = convertedValue;
    parameterTypes.value[newParameterKey.value] = newParameterType.value;
  }
  
  isNewParameter.value = false;
  newParameterKey.value = '';
  newParameterValue.value = '';
  newParameterType.value = 'String';
}

const cancelAddParameter = () => {
  isNewParameter.value = false;
  newParameterKey.value = '';
  newParameterValue.value = '';
}

const handleDeleteParameter = (key: string) => {
  console.log('handleDeleteParameter', key);
  if (parsedParameters.value) {
    const copy = { ...parsedParameters.value };
    delete copy[key];
    delete parameterTypes.value[key];
    parsedParameters.value = copy;
  }
}

const handleSaveAllParameters = async () => {
  const jobIdToSave = currentJobIdForModal.value;
  
  if (jobIdToSave === null) {
    console.error("Cannot save parameters, Job ID is missing.");
    alert("저장 중 오류 발생: Job ID를 찾을 수 없습니다.");
    return;
  }

  try {
    const response = await axios.post('/batch/update/parameter', {
      id: jobIdToSave,
      jobParameters: parsedParameters.value
    });

    if (response.data && response.data.success) {
      alert('파라미터가 성공적으로 저장되었습니다.');
      closeParametersModal();
    } else {
      alert('파라미터 저장에 실패했습니다.');
    }
  } catch (error) {
    console.error("Error saving parameters:", error);
    alert('파라미터 저장 중 오류가 발생했습니다.');
  }
}

const updateParameterType = (key: string) => {
  if (parsedParameters.value && key in parsedParameters.value) {
    const currentValue = parsedParameters.value[key];
    parsedParameters.value[key] = convertValueByType(currentValue, parameterTypes.value[key]);
  }
}

const updateParameterValue = (key: string, event: Event) => {
  const target = event.target as HTMLInputElement;
  if (parsedParameters.value) {
    const value = target.value;
    
    if (parameterTypes.value[key] === 'Number') {
      if (isNaN(Number(value))) {
        alert('숫자만 입력 가능합니다.');
        return;
      }
    }
    
    parsedParameters.value[key] = convertValueByType(value, parameterTypes.value[key]);
  }
}

const handleExecuteJob = async (jobId: number) => {
  try {
    const response = await axios.post(`/batch/execute/${jobId}`);
    if (response.data && response.data.success) {
      alert('작업이 성공적으로 실행 요청되었습니다.');
      // Optionally, refresh the table or update UI
      // fetchBatchJobs(); 
    } else {
      alert(response.data?.message || '작업 실행 요청에 실패했습니다.');
    }
  } catch (error: any) {
    alert(error.response?.data?.message || '작업 실행 중 오류가 발생했습니다.');
  }
};

onMounted(() => {
  fetchBatchJobs(currentPage.value, pageSize.value);
  // 이벤트 위임 추가 (동적으로 생성된 버튼 대응)
  $(document).on('click', 'a.action-view-parameters', function (e) {
    e.preventDefault();
    const jobId = $(this).data('job-id');
    if (jobId) {
      const job = batchJobs.value.find(j => j.id === jobId);
      if (job) {
        handleEdit(job);
      }
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

/* 모달 스타일 추가 */
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
  /*justify-content: flex-end; /* Aligns group to the right if needed, but DataTables places it */
}

#customSearchContainer .custom-search-input {
  /* Adjust width as needed */
  /* flex-grow: 1; /* Allows input to take available space */
}

#customSearchContainer .custom-search-button {
  /* margin-left: 0.5rem; */
}

#customSearchContainer .custom-add-button {
  /* margin-left: 0.5rem; /* Spacing from search button */
}
</style>
