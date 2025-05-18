<template>
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-xl-10 col-lg-12 col-md-9">
        <div class="card o-hidden border-0 shadow-lg my-5">
          <div class="card-body p-0">
            <div class="row">
              <div class="col-lg-6 d-none d-lg-block bg-login-image"></div>
              <div class="col-lg-6">
                <div class="p-5">
                  <div class="text-center">
                    <h1 class="h4 text-gray-900 mb-4">Welcome Back!</h1>
                  </div>
                  <form @submit.prevent="handleLogin" class="user">
                    <div class="form-group">
                      <input
                        type="email"
                        class="form-control form-control-user"
                        placeholder="Enter Email Address..."
                        v-model="email"
                        required
                      />
                    </div>
                    <div class="form-group">
                      <input
                        type="password"
                        class="form-control form-control-user"
                        placeholder="Password"
                        v-model="password"
                        required
                      />
                    </div>
                    <button type="submit" class="btn btn-primary btn-user btn-block" :disabled="loading">
                      <span v-if="loading">로그인 중...</span>
                      <span v-else>Login</span>
                    </button>
                  </form>
                  <hr />
                  <div class="text-center">
                    <a class="small" href="#">Forgot Password?</a>
                  </div>
                  <div class="text-center">
                    <a class="small" href="#">Create an Account!</a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>  
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      password: password.value,
    })
    console.log(response);
    if (response.data && response.data.data.accessToken) {
      authStore.login(response.data.data.accessToken)
      router.push('/dashboard')
    } else {
      alert('로그인 응답에 토큰이 없습니다. 백엔드 응답 구조를 확인하세요.')
      console.error('로그인 응답:', response.data)
    }
  } catch (error: any) {
    alert('로그인 실패: 아이디 또는 비밀번호를 확인하세요.')
    console.error('로그인 오류:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.bg-login-image {
  background: url('/img/bg-login.jpg');
  background-position: center;
  background-size: cover;
}
</style>
