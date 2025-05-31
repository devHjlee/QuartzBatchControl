// jQuery와 Bootstrap JS (Popper.js 포함)
import jquery from 'jquery'; // 'jquery'를 jquery라는 이름으로 가져옴
(window as any).jQuery = jquery; // window.jQuery에 할당
(window as any).$ = jquery; // window.$에 할당 (많은 라이브러리가 $를 사용)

import 'popper.js'; // Bootstrap 이전에 임포트
import 'bootstrap'; // Bootstrap JS

// DataTables JS (jQuery와 Bootstrap에 의존하므로, 관련 JS 로드 후 임포트)
import 'datatables.net'; // DataTables 코어 JS
import 'datatables.net-bs4'; // DataTables Bootstrap 4 통합 JS

import Chart from 'chart.js/auto'; // Revert to chart.js/auto
(window as any).Chart = Chart; // 전역으로 Chart 객체 할당

// Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css';

// Font Awesome
import '@fortawesome/fontawesome-free/css/all.min.css';

// SB Admin 2 Theme CSS
import '@/assets/sb-admin-2/css/sb-admin-2.min.css';

// DataTables CSS (Bootstrap 4용)
import 'datatables.net-bs4/css/dataTables.bootstrap4.min.css';

// Chart.js (필요하다면)
// import 'chart.js'; // Chart.js는 보통 컴포넌트 레벨에서 import하거나, 전역으로 사용하려면 여기서 import

// SB Admin 2 Theme JS (jQuery와 Bootstrap이 로드된 후)
// 이 파일이 전역 jQuery($)를 사용하므로, jQuery가 먼저 import 되어야 합니다.
// 또한, 이 JS가 DOM 요소를 조작한다면 Vue 앱이 마운트된 후에 실행되도록 하거나,
// Vue 방식으로 로직을 재작성하는 것을 고려해야 합니다.
// 우선은 import 해봅니다.
// import '@/assets/sb-admin-2/vendor/jquery/jquery.min.js'
// import '@/assets/sb-admin-2/vendor/datatables/dataTables.bootstrap4.css'

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
