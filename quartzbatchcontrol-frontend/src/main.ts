import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import '@/assets/main.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/assets/sb-admin-2/css/sb-admin-2.min.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')