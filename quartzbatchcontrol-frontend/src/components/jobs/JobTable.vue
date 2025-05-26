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
                <th class="center-text">Next Fire Time</th>
                <th class="center-text">Prev Fire Time</th>
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

  </div>
  <!-- /.container-fluid -->
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
  createdBy: string;
}

const quartzJobs = ref<QuartzJobInfo[]>([])
const dataTable = ref<any>(null)
const searchInputValue = ref(''); // To store search input

const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString()
}

const fetchquartzJobs = async () => {
  try {
    if (dataTable.value) {
      dataTable.value.destroy()
    }

    dataTable.value = window.jQuery('#quartzMetaTable').DataTable({
      paging: true,
      searching: false, // Disable DataTables default search, we'll use a custom one
      info: true,
      responsive: true,
      serverSide: true,
      processing: true,
      pageLength: 10, // 기본 페이지 크기를 API의 기본값과 유사하게 설정하거나, API에서 size를 받도록 수정
      lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "전체"]], // API의 size 파라미터와 일치시키거나 유연하게 처리
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
        axios.get('/quartz-jobs', { // API 엔드포인트 확인 필요
          params: {
            page: dtParams.start / dtParams.length,
            size: dtParams.length,
            jobName: searchInputValue.value, // 검색 파라미터 수정
            // metaName: searchInputValue.value, // metaName 파라미터는 새 API 응답에 없으므로 제거
          }
        })
        .then(function (response) {
          // API 응답 구조에 맞춰 수정
          const backendResponse = response.data;
          if (backendResponse.success && backendResponse.data && backendResponse.data.content && backendResponse.data.page) {
            quartzJobs.value = backendResponse.data.content;
            callback({
              draw: dtParams.draw,
              recordsTotal: backendResponse.data.page.totalElements,
              recordsFiltered: backendResponse.data.page.totalElements, // 검색 결과에 따라 이 값을 조정해야 할 수 있음
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
        {
          data: 'nextFireTime',
          className: 'text-center',
          render: function(data, type, row) {
            return formatTime(data);
          }
        },
        {
          data: 'prevFireTime',
          className: 'text-center',
          render: function(data, type, row) {
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
            // 수정 버튼은 handleEdit 함수가 제거되었으므로 일단 제거
            // <button class="btn btn-sm btn-primary action-edit me-1" data-job-id="${row.id}">수정</button>
            return `
              <button class="btn btn-sm btn-info action-execute" data-job-id="${row.id}">실행</button>
            `;
          }
        }
      ],
      initComplete: function() {
        const customSearchContainer = document.getElementById('customSearchContainer');
        if (customSearchContainer) {
          // 추가 버튼은 handleAddNew 함수가 제거되었으므로 일단 제거
          customSearchContainer.innerHTML = `
            <div class="input-group">
              <input type="text" class="form-control custom-search-input" placeholder="Job Name 검색">
              <button class="btn btn-outline-secondary custom-search-button" type="button">검색</button>
            </div>
          `;
          // <button class="btn btn-primary ms-2 custom-add-button" type="button">추가</button>

          const searchInput = customSearchContainer.querySelector('.custom-search-input');
          const searchButton = customSearchContainer.querySelector('.custom-search-button');
          // const addButton = customSearchContainer.querySelector('.custom-add-button'); // addButton 관련 로직 제거

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
            // addButton 관련 로직 제거
            // if (addButton) {
            //   addButton.addEventListener('click', () => {
            //     // handleAddNew(); // handleAddNew 함수가 제거됨
            //   });
            // }
          }
        }
      }
    });

    // 수정 버튼 이벤트 바인딩 (제거됨)
    // $(document).off('click', '#quartzMetaTable button.action-edit').on('click', '#quartzMetaTable button.action-edit', function (e) {
    //   e.preventDefault();
    //   const jobId = $(this).data('job-id');
    //   const job = quartzJobs.value.find(j => j.id === jobId);
    //   if (job) {
    //     // handleEdit(job); // handleEdit 함수가 제거됨
    //   }
    // });

    // 실행 버튼 이벤트 바인딩
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
    console.error('배치 작업 조회 또는 DataTable 초기화 실패', err)
  }
}

const handleExecuteJob = async (jobId: number) => {
  try {
    const response = await axios.post(`/batch/execute/${jobId}`); // API 엔드포인트 확인 필요
    if (response.data && response.data.success) {
      alert('작업이 성공적으로 실행 요청되었습니다.');
      // Optionally, refresh the table or update UI
      if (dataTable.value) {
        dataTable.value.ajax.reload(null, false); // 현재 페이징 유지하며 리로드
      }
    } else {
      alert(response.data?.message || '작업 실행 요청에 실패했습니다.');
    }
  } catch (error: any) {
    alert(error.response?.data?.message || '작업 실행 중 오류가 발생했습니다.');
  }
};

onMounted(() => {
  fetchquartzJobs();
  // 모달 관련 이벤트 위임 제거
  // $(document).on('click', 'a.action-view-parameters', function (e) {
  //   e.preventDefault();
  //   const jobId = $(this).data('job-id');
  //   if (jobId) {
  //     const job = quartzJobs.value.find(j => j.id === jobId);
  //     if (job) {
  //       // handleEdit(job); // handleEdit 함수가 제거됨
  //     }
  //   }
  // });
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

/* 모달 관련 스타일 제거됨 */

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
  /*justify-content: flex-end; /* Aligns group to the right if needed, but DataTables places it */
}

#customSearchContainer .custom-search-input {
  /* Adjust width as needed */
  /* flex-grow: 1; /* Allows input to take available space */
}

#customSearchContainer .custom-search-button {
  /* margin-left: 0.5rem; */
}

/* custom-add-button 관련 스타일 제거됨 */
</style>
