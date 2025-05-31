// jQuery와 Bootstrap JS (Popper.js 포함)
import jquery from 'jquery'; // 'jquery'를 jquery라는 이름으로 가져옴
(window as any).jQuery = jquery; // window.jQuery에 할당
(window as any).$ = jquery; // window.$에 할당 (많은 라이브러리가 $를 사용)

import 'popper.js'; // Bootstrap 이전에 임포트
import 'bootstrap'; // Bootstrap JS

// DataTables JS (jQuery와 Bootstrap에 의존하므로, 관련 JS 로드 후 임포트)
import 'datatables.net'; // DataTables 코어 JS
import 'datatables.net-bs4'; // DataTables Bootstrap 4 통합 JS

import Chart from 'chart.js/auto'; // 임시 주석 처리 -> 주석 해제
(window as any).Chart = Chart; // 임시 주석 처리 -> 주석 해제

// CSS 임포트 순서 조정 시작
import 'bootstrap/dist/css/bootstrap.min.css'; // 1. Bootstrap 기본
import '@fortawesome/fontawesome-free/css/all.min.css'; // 2. Font Awesome
import 'datatables.net-bs4/css/dataTables.bootstrap4.min.css'; // 3. DataTables Bootstrap 4 테마
import '@/assets/sb-admin-2/css/sb-admin-2.min.css'; // 4. SB Admin 2 테마

// Chart.js CSS는 Chart.js가 직접 로드하거나, 필요시 여기에 추가

// 이전에 주석 처리된 SB Admin 2 Theme JS 로드 부분은 App.vue에서 동적으로 로드 중이므로 여기서는 주석 유지
// import '@/assets/sb-admin-2/js/sb-admin-2.min.js';

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'

// import '@/assets/sb-admin-2/vendor/jquery/jquery.min.js'
// import '@/assets/sb-admin-2/vendor/datatables/dataTables.bootstrap4.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

const authStore = useAuthStore(pinia)
authStore.initializeAuth()

app.use(router)
app.mount('#app')
