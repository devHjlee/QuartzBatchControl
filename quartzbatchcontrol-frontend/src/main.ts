import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import '@/assets/main.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/assets/sb-admin-2/vendor/fontawesome-free/css/all.min.css'
import '@/assets/sb-admin-2/css/sb-admin-2.min.css'



// Bootstrap JS와 sb-admin-2 JS
import 'bootstrap'


const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')