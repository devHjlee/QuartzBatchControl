<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">Batch Log 관리</h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table id="batchLogTable" class="table table-bordered" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th class="center-text">Job Name</th>
                <th class="center-text">Instance ID</th>
                <th class="center-text">Status</th>
                <th class="center-text">Start Time</th>
                <th class="center-text">End Time</th>
                <th class="center-text">Message</th>
                <!-- <th class="center-text">Action</th> -->
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

interface BatchLogItem {
  id: number // Assuming there's an ID for each log entry
  jobName: string
  jobInstanceId: string // Or number, depending on backend
  status: string
  startTime: string // ISO string or similar, will be formatted
  endTime: string   // ISO string or similar, will be formatted
  exitMessage: string
  // Add other relevant log fields based on BatchLogResponse
}

interface PageResponse {
  content: BatchLogItem[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

const batchLogs = ref<BatchLogItem[]>([])
const dataTable = ref<any>(null)
// const currentPage = ref(0) // Not directly used with server-side DataTable's own paging
// const pageSize = ref(10) // DataTable's pageLength handles this

const searchInputValue = ref(''); // To store search input

const formatTime = (time: string): string => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const fetchBatchLogs = async () => {
  try {
    await nextTick()

    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = window.jQuery('#batchLogTable').DataTable({
      paging: true,
      searching: false, // Disable DataTables default search, custom one is used
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      // pageLength: pageSize.value, // Uses DataTable's default or what's selected in lengthMenu
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
      dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"<"#customSearchContainer">>><"row"<"col-sm-12"tr>><"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
      ordering: false,
      language: {
        emptyTable: '조회된 로그가 없습니다.',
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
        axios.get('/batch-log', { // Updated API endpoint
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            keyword: searchInputValue.value, // Assuming backend supports 'keyword' for search
            // Add other BatchLogSearchRequest params if needed
          }
        })
        .then(function (response) {
          const backendPayload = response.data.data;
          // Assuming backend response structure is { data: { content: [], page: { totalElements: ... } } }
          // or { data: { content: [], totalElements: ... } } if PagedModel is flattened
          let content = [];
          let recordsTotal = 0;

          if (backendPayload && Array.isArray(backendPayload.content)) { // Standard Spring Page
            content = backendPayload.content;
            recordsTotal = backendPayload.totalElements; // If page object is not nested
             if (backendPayload.page && typeof backendPayload.page.totalElements !== 'undefined') { // If PagedModel structure
                recordsTotal = backendPayload.page.totalElements;
             } else if (typeof backendPayload.totalElements !== 'undefined') { // If page object is flat
                recordsTotal = backendPayload.totalElements;
             }

            batchLogs.value = content;
            callback({
              draw: dtParams.draw,
              recordsTotal: recordsTotal,
              recordsFiltered: recordsTotal, // Assuming no separate filtering count from backend for now
              data: content
            });
          } else if (backendPayload && Array.isArray(backendPayload.content) && backendPayload.page) { // PagedModel structure
             batchLogs.value = backendPayload.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendPayload.page.totalElements,
              recordsFiltered: backendPayload.page.totalElements,
              data: backendPayload.content
            });
          }
           else {
            console.warn('Unexpected backend payload structure:', backendPayload);
            callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
          }
        })
        .catch(function (error) {
          console.error('Error fetching batch logs:', error);
          callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
        });
      },
      columns: [
        { data: 'jobName', defaultContent: '-' },
        { data: 'jobInstanceId', defaultContent: '-', className: 'center-text' },
        { data: 'status', defaultContent: '-', className: 'center-text' },
        { data: 'startTime', defaultContent: '-', className: 'center-text', render: data => formatTime(data) },
        { data: 'endTime', defaultContent: '-', className: 'center-text', render: data => formatTime(data) },
        { data: 'exitMessage', defaultContent: '-' },
        // { // Action column removed for now, can be added back if specific log actions are needed
        //   data: null,
        //   orderable: false,
        //   searchable: false,
        //   className: 'text-center',
        //   render: function(data, type, row) {
        //     // Add buttons for log-specific actions if any
        //     return '';
        //   }
        // }
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customSearchContainer');
        if (customSearchContainer) {
          customSearchContainer.innerHTML = `
            <div class="input-group">
              <input type="text" class="form-control custom-search-input" placeholder="검색 (Job Name, Message 등)">
              <button class="btn btn-outline-secondary custom-search-button" type="button">검색</button>
              <!-- Add button removed -->
            </div>
          `;

          const searchInput = customSearchContainer.querySelector('.custom-search-input');
          const searchButton = customSearchContainer.querySelector('.custom-search-button');

          if (searchInput instanceof HTMLInputElement && searchButton) {
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
:deep(.dataTables_filter label) {
  margin-bottom: 0; /* Align items vertically if they wrap */
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
