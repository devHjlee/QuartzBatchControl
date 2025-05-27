<template>
  <!-- Begin Page Content -->
  <div class="container-fluid">
    <!-- DataTales Example -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">Quartz 실행 로그</h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table id="quartzLogTable" class="table table-bordered" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th class="center-text">Log ID</th>
                <th class="center-text">Job Name</th>
                <th class="center-text">Job Group</th>
                <th class="center-text">Start Time</th>
                <th class="center-text">End Time</th>
                <th class="center-text">Status</th>
                <th class="center-text">Message</th>
              </tr>
            </thead>
            <tbody>
              <!-- DataTables will populate this section -->
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- Modal for adding/editing Quartz Jobs REMOVED -->
  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'; // watch, useRoute, useRouter might not be needed for basic log display
import axios from '@/api/axios';
import $ from 'jquery';

// import { useRoute, useRouter } from 'vue-router'; // Removed, assuming not directly needed for log display logic here

interface QuartzLogItem { // Based on QuartzLogResponse.java
  id: number;
  jobName: string;
  jobGroup: string;
  startTime: string; // Assuming backend sends as ISO string or similar
  endTime: string;   // Assuming backend sends as ISO string or similar
  status: string;
  message: string;
}

// Interface for the backend's page response structure for Quartz Logs
interface QuartzLogPageResponse {
  content: QuartzLogItem[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // Current page number
}

const quartzLogs = ref<QuartzLogItem[]>([]);
const dataTable = ref<any>(null);
const searchInputValue = ref(''); // For searching logs

// const route = useRoute(); // Removed, re-evaluate if specific routing logic is needed for logs
// const router = useRouter(); // Removed

const formatTime = (time: string): string => { // Parameter type changed to string if backend sends ISO string
  if (!time) return '-';
  return new Date(time).toLocaleString();
};

const fetchQuartzLogs = async () => {
  try {
    await nextTick(); // Ensures DOM is ready if DataTable needs to attach to an existing element

    if (dataTable.value) {
      dataTable.value.destroy();
    }

    dataTable.value = window.jQuery('#quartzLogTable').DataTable({
      paging: true,
      searching: false, // Using custom search input
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      ordering: false,
      pageLength: 10,
      lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "전체"]],
      dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"<"#customLogSearchContainer">>><"row"<"col-sm-12"tr>><"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
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
        axios.get('/quartz-log', { // API endpoint for Quartz Logs
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            keyword: searchInputValue.value, // Assuming QuartzLogSearchRequest uses 'keyword'
          }
        })
        .then(function (response) {
          const backendResponse = response.data; //  { success: boolean, data: PagedModel<QuartzLogResponse>, message: string }
          if (backendResponse.success && backendResponse.data && backendResponse.data.content && backendResponse.data.page) {
            quartzLogs.value = backendResponse.data.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendResponse.data.page.totalElements,
              recordsFiltered: backendResponse.data.page.totalElements, // Assuming search is backend-side
              data: backendResponse.data.content
            });
          } else {
            console.error("Invalid API response structure for Quartz logs:", backendResponse);
            callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
          }
        })
        .catch(function (error) {
          console.error("API error fetching Quartz logs:", error);
          callback({ draw: dtParams.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
        });
      },
      columns: [
        { data: 'id', className: 'center-text' },
        { data: 'jobName' },
        { data: 'jobGroup', className: 'center-text' },
        { data: 'startTime', className: 'center-text', render: data => formatTime(data) },
        { data: 'endTime', className: 'center-text', render: data => formatTime(data) },
        { data: 'status', className: 'center-text' },
        { data: 'message', defaultContent: '-' }
        // Action column removed as logs are typically view-only
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customLogSearchContainer');
        if (customSearchContainer) {
          customSearchContainer.innerHTML = `
            <div class="input-group">
              <input type="text" class="form-control custom-search-input" placeholder="Job Name, Message 검색">
              <button class="btn btn-outline-secondary custom-search-button" type="button">검색</button>
              <!-- Add button removed -->
            </div>
          `;

          const searchInput = customSearchContainer.querySelector('.custom-search-input');
          const searchButton = customSearchContainer.querySelector('.custom-search-button');

          if (searchInput instanceof HTMLInputElement && searchButton) {
            // Restore searchInputValue if it was previously set (e.g. from URL query)
            if(searchInputValue.value) {
                searchInput.value = searchInputValue.value;
            }
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

    // Removed all jQuery event handlers for job actions (trigger, resume, pause, delete, edit)

  } catch (err) {
    console.error('Quartz 로그 조회 또는 DataTable 초기화 실패', err);
  }
};

onMounted(() => {
  // If there was a search term from BatchTable.vue, it would be in route.query.
  // This part needs to be handled carefully if we want to persist search terms across navigation.
  // For now, we assume `searchInputValue` might be pre-filled if JobLogView handled it.
  // Or, we can explicitly check route.query here as well if this component is directly routed to with search params.

  // Example: if (route.query.searchFromBatchTable && typeof route.query.searchFromBatchTable === 'string') {
  //  searchInputValue.value = route.query.searchFromBatchTable;
  //  // Optionally, clear the query from URL after using it
  //  const currentPath = router.currentRoute.value.path;
  //  const currentQuery = { ...router.currentRoute.value.query };
  //  delete currentQuery.searchFromBatchTable;
  //  router.replace({ path: currentPath, query: currentQuery });
  // }

  fetchQuartzLogs();

  // Removed watch for route.query.searchFromBatchTable as its logic should be simplified or handled by the parent view if necessary.
});

// All functions related to Quartz Job Add/Edit/Actions (fetchAvailableBatchMetas, openAddQuartzJobModal, etc.) have been removed.
</script>

<style scoped>
.center-text {
  text-align: center;
}

.table-responsive {
  overflow-x: auto;
}

.me-2 {
  margin-right: 0.5rem !important;
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

/* Modal related styles removed */

/* Custom search styles */
#customLogSearchContainer .input-group {}

#customLogSearchContainer .custom-search-input {}
</style>
