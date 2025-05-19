import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const instance = axios.create({
  baseURL: '/',
})

instance.interceptors.request.use(config => {
  const authStore = useAuthStore()
  const token = authStore.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default instance
