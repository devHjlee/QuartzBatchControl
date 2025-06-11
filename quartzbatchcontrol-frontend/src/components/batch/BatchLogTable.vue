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

    <!-- Batch Job Modal REMOVED -->

  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import axios from '@/api/axios'
import $ from 'jquery'
// import { useRouter } from 'vue-router' // No longer needed for navigation from this table

// BatchLogResponse.java 에 맞춘 인터페이스
interface BatchLogItem {
  id: number;
  runId: String;
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
      }
    });

    // Removed event handlers for edit, execute, view-job as they are not relevant for logs

  } catch (err) {
    console.error('배치 로그 조회 또는 DataTable 초기화 실패', err)
  }
}

// const router = useRouter() // No longer needed

onMounted(() => {
  fetchBatchLogs();
  // Removed event delegation for 'a.action-view-parameters'
});
</script>

<style scoped>
.center-text {
  text-align: center;
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
</style>
