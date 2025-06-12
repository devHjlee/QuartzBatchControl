<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">배치 실행 로그</h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table id="batchLogTable" class="table table-bordered" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th class="center-text">ID</th>
                <th class="center-text">Job Name</th>
                <th class="center-text">Batch Name</th>
                <th class="center-text">Status</th>
                <th class="center-text">Start Time</th>
                <th class="center-text">End Time</th>
                <th class="center-text">Exit Code</th>
                <th class="center-text">Exit Message</th>
                <!-- <th class="center-text">Parameters</th> -->
                <!-- <th class="center-text">Job Execution ID</th> -->
                <!-- <th class="center-text">Meta ID</th> -->
              </tr>
            </thead>
            <tbody>
              <!-- DataTables will populate this section -->
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Log Viewer Modal -->
    <div class="modal fade" id="logViewerModal" tabindex="-1" aria-labelledby="logViewerModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="logViewerModalLabel">배치 실행 로그</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <pre v-html="styledLogContent"></pre>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">닫기</button>
            <button type="button" class="btn btn-primary" @click="downloadLog">다운로드</button>
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
import 'datatables.net-bs4'; // Import DataTables with Bootstrap 4 styling
import * as bootstrap from 'bootstrap'; // Import bootstrap for modal control
// import { useRouter } from 'vue-router' // No longer needed for navigation from this table

// BatchLogResponse.java 에 맞춘 인터페이스
interface BatchLogItem {
  id: number;
  runId: string; // runId는 string입니다.
  filePath: String;
  jobExecutionId: number | null;
  jobName: string;
  metaId: number | null;
  batchName: string | null;
  startTime: string;
  endTime: string | null;
  status: string;
  exitCode: string | null;
  exitMessage: string | null;
  jobParameters: string | null;
}

// Spring의 Page 응답 또는 PagedModel을 위한 인터페이스
interface SpringPage {
  content: BatchLogItem[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 현재 페이지 (0-indexed)
  // PagedModel의 경우 metadata.number, metadata.size 등으로 접근할 수 있음
}

const batchLogs = ref<BatchLogItem[]>([])
const dataTable = ref<any>(null)
// const currentPage = ref(0) // Not directly used with server-side DataTable's own paging
// const pageSize = ref(10) // DataTable's pageLength handles this

const searchKeyword = ref('')
const searchStatus = ref('') // Status 검색을 위한 ref

// --- 모달 관련 상태 ---
const logContent = ref('');
const currentLogRunId = ref('');
let logModal: bootstrap.Modal | null = null;

const styledLogContent = computed(() => {
  if (!logContent.value) {
    return '';
  }
  return logContent.value
    .split('\n')
    .map((line) => {
      let escapedLine = escapeHtml(line);
      // Highlight keywords
      escapedLine = escapedLine.replace(/\b(ERROR)\b/g, '<strong class="log-error">$1</strong>');
      escapedLine = escapedLine.replace(/\b(WARN|WARNING)\b/g, '<strong class="log-warn">$1</strong>');
      escapedLine = escapedLine.replace(/\b(INFO)\b/g, '<strong class="log-info">$1</strong>');
      escapedLine = escapedLine.replace(/\b(DEBUG)\b/g, '<strong class="log-debug">$1</strong>');
      return escapedLine;
    })
    .join('<br />');
});

const formatTime = (time: string | null): string => {
  if (!time) return '-'
  try {
    return new Date(time).toLocaleString()
  } catch (e) {
    console.error('Invalid time format:', time, e)
    return 'Invalid Date'
  }
}

const escapeHtml = (unsafe: string | null): string => {
  if (unsafe === null || typeof unsafe === 'undefined') return '';
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

const fetchBatchLogs = async () => {
  try {
    await nextTick()

    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = (window as any).jQuery('#batchLogTable').DataTable({
      paging: true,
      searching: false, // DataTables 자체 검색 비활성화
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      // pageLength: pageSize.value, // Uses DataTable's default or what's selected in lengthMenu
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
      dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"<"#customSearchContainer">>><"row"<"col-sm-12"tr>><"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
      ordering: false, // 정렬 기능 비활성화
      language: {
        emptyTable: '조회된 로그가 없습니다.',
        lengthMenu: '_MENU_개씩 보기',
        zeroRecords: '검색 결과가 없습니다.',
        info: '총 _TOTAL_건 중 _START_~_END_ 표시',
        infoEmpty: '표시할 데이터가 없습니다.',
        infoFiltered: '(전체 _MAX_건에서 검색됨)',
        paginate: {
          first: '처음',
          last: '마지막',
          next: '다음',
          previous: '이전',
        },
        processing: '데이터를 불러오는 중...',
      },
      ajax: function (dtParams: any, callback: Function, settings: any) {
        axios.get('/batch-log', {
          params: {
            page: dtParams.start / dtParams.length, // 0-indexed page
            size: dtParams.length,
            keyword: searchKeyword.value,
            status: searchStatus.value,
          }
        })
        .then(function (response) {
          // JobLogTable.vue의 응답 처리 방식과 유사하게 수정
          const backendResponse = response.data;

          if (backendResponse.success && backendResponse.data && backendResponse.data.content && backendResponse.data.page) {
            batchLogs.value = backendResponse.data.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendResponse.data.page.totalElements,
              recordsFiltered: backendResponse.data.page.totalElements,
              data: backendResponse.data.content
            });
          } else {
            console.warn('Unexpected backend payload structure for batch logs:', backendResponse);
            callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
          }
        })
        .catch(function (error) {
          console.error('Error fetching batch logs:', error);
          callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
        });
      },
      columns: [
        { data: 'id', title: 'ID', className: 'center-text', visible: false },
        { data: 'jobName', title: 'Job Name', defaultContent: '-' },
        { data: 'batchName', title: 'Batch Name', defaultContent: '-' },
        { data: 'status', title: 'Status', className: 'center-text' },
        { data: 'startTime', title: 'Start Time', className: 'center-text', render: (data: any) => formatTime(data) },
        { data: 'endTime', title: 'End Time', className: 'center-text', render: (data: any) => formatTime(data) },
        { data: 'exitCode', title: 'Exit Code', defaultContent: '-', className: 'center-text' },
        {
          data: 'exitMessage',
          title: 'Exit Message',
          defaultContent: '-',
          render: function(data: any, type: any, row: any) {
            if (type === 'display' && data && data.length > 50) {
              return escapeHtml(data.substr(0, 50)) + '...';
            }
            return escapeHtml(data);
          }
        },
        // { data: 'jobParameters', title: 'Parameters', defaultContent: '-' },
        // { data: 'jobExecutionId', title: 'Job Execution ID', defaultContent: '-' },
        // { data: 'metaId', title: 'Meta ID', defaultContent: '-' },
        // 로그 보기 버튼을 위한 새로운 컬럼 추가
        {
            data: 'runId',
            title: 'Log',
            className: 'center-text',
            orderable: false,
            render: function(data: any, type: any, row: any) {
                // data-run-id 속성에 runId를 저장하여 이벤트 핸들러에서 사용
                return `<button class="btn btn-primary btn-sm action-view-log" data-run-id="${data}">보기</button>`;
            }
        }
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customSearchContainer');
        if (customSearchContainer) {
          customSearchContainer.classList.remove('col-sm-12', 'col-md-6');
          customSearchContainer.innerHTML =
            '<div class="input-group">' +
              '<select class="form-control custom-status-select me-2" style="width: auto; flex-grow: 0.3;">' +
                '<option value="">All Statuses</option>' +
                '<option value="COMPLETED">COMPLETED</option>' +
                '<option value="FAILED">FAILED</option>' +
              '</select>' +
              '<input type="text" class="form-control custom-keyword-input" placeholder="Keyword 검색 (Job Name, Batch Name 등)">' +
              '<button class="btn btn-outline-secondary custom-search-button" type="button">검색</button>' +
            '</div>';

          const statusSelect = customSearchContainer.querySelector('.custom-status-select') as HTMLSelectElement;
          const keywordInput = customSearchContainer.querySelector('.custom-keyword-input') as HTMLInputElement;
          const searchButton = customSearchContainer.querySelector('.custom-search-button');

          if (statusSelect && keywordInput && searchButton) {
            statusSelect.addEventListener('change', () => {
              searchStatus.value = statusSelect.value;
              dataTable.value.ajax.reload();
            });
            keywordInput.addEventListener('keyup', (event) => {
              if (event.key === 'Enter') {
                searchKeyword.value = keywordInput.value;
                searchStatus.value = statusSelect.value;
                dataTable.value.ajax.reload();
              }
            });
            searchButton.addEventListener('click', () => {
              searchKeyword.value = keywordInput.value;
              searchStatus.value = statusSelect.value;
              dataTable.value.ajax.reload();
            });
          }
        }
      },
      // DataTables가 행을 다시 그릴 때마다 호출
      drawCallback: function() {
        // 기존 이벤트 핸들러 제거 (중복 방지)
        $('#batchLogTable tbody').off('click', 'button.action-view-log');
        
        // 새로 그려진 버튼에 이벤트 핸들러 바인딩
        $('#batchLogTable tbody').on('click', 'button.action-view-log', function() {
          const runId = $(this).data('run-id');
          if (runId) {
            showLog(runId);
          }
        });
      }
    });

    // Removed event handlers for edit, execute, view-job as they are not relevant for logs

  } catch (err) {
    console.error('배치 로그 조회 또는 DataTable 초기화 실패', err)
  }
}

// 로그 보기 버튼 클릭 시 실행될 함수
const showLog = async (runId: string) => {
  try {
    logContent.value = '로그를 불러오는 중...';
    currentLogRunId.value = runId;
    if(logModal) {
      logModal.show();
    }
    
    const response = await axios.get(`/batch-log/${runId}/content`);
    logContent.value = response.data;
  } catch (error) {
    console.error('로그 내용을 불러오는데 실패했습니다:', error);
    logContent.value = '로그를 불러오는데 실패했습니다. 서버 오류를 확인하세요.';
  }
};

// 로그 다운로드 함수
const downloadLog = () => {
  if (!logContent.value) return;
  const blob = new Blob([logContent.value], { type: 'text/plain;charset=utf-8' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `batch-log-${currentLogRunId.value}.log`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
};

// const router = useRouter() // No longer needed

onMounted(() => {
  fetchBatchLogs();
  const modalElement = document.getElementById('logViewerModal');
  if (modalElement) {
    logModal = new bootstrap.Modal(modalElement);
  }
  // Removed event delegation for 'a.action-view-parameters'
});
</script>

<style scoped>
.center-text {
  text-align: center;
  vertical-align: middle; /* 버튼이 가운데 오도록 */
}

.table-responsive {
  overflow-x: auto;
}

/* .btn-sm, .me-1 styles might not be needed if action buttons are removed */

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
/* :deep(.dataTables_filter label) { Default filter is hidden */
:deep(#customSearchContainer .form-label) { /* If using labels in custom search */
  margin-bottom: 0;
}

/* Custom search styles */
:deep(#customSearchContainer .input-group) {
  width: 100% !important;
  display: flex !important;
}

:deep(#customSearchContainer .custom-keyword-input) { /* BatchLogTable은 custom-keyword-input 사용 */
  flex-grow: 1 !important;
}

:deep(#customSearchContainer .custom-status-select),
:deep(#customSearchContainer .custom-search-button) {
  flex-grow: 0;
  flex-shrink: 0;
}

/* Modal styles removed */

/* .btn-circle styles might not be needed if action buttons are removed */

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

/* #customSearchContainer .custom-search-button,
#customSearchContainer .custom-add-button styles might not be needed or can be simplified */

/* 모달의 pre 태그 스타일 */
.modal-body pre {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 0.25rem;
  max-height: 60vh;
  overflow-y: auto;
  white-space: pre-wrap;   /* 자동 줄바꿈 */
  word-wrap: break-word;   /* 긴 단어 강제 줄바꿈 */
}

/* Log level colors */
:deep(.log-error) {
  color: #dc3545; /* Bootstrap's danger color */
}
:deep(.log-warn) {
  color: #ffc107; /* Bootstrap's warning color */
}
:deep(.log-info) {
  color: #17a2b8; /* Bootstrap's info color */
}
:deep(.log-debug) {
  color: #6c757d; /* Bootstrap's secondary color */
}
</style>
