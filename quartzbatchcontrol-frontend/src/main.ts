import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'

// import '@/assets/sb-admin-2/vendor/jquery/jquery.min.js'
import '@/assets/sb-admin-2/css/sb-admin-2.min.css'
// import '@/assets/sb-admin-2/vendor/datatables/dataTables.bootstrap4.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

const authStore = useAuthStore(pinia)
authStore.initializeAuth()

app.use(router)
app.mount('#app')
