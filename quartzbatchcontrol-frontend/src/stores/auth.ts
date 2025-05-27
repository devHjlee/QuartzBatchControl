import { defineStore } from 'pinia'

interface AuthState {
  isLoggedIn: boolean
  token: string | null
  user: any | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    isLoggedIn: false,
    token: null,
    user: null
  }),

  getters: {
    getIsLoggedIn: (state) => state.isLoggedIn,
    getToken: (state) => state.token,
    getUser: (state) => state.user
  },

  actions: {
    login(token: string, user: any = null) {
      this.token = token
      this.isLoggedIn = true
      this.user = user
      // 토큰을 localStorage에 저장
      localStorage.setItem('token', token)
    },
    setLoginState(isLoggedIn: boolean) {
      this.isLoggedIn = isLoggedIn
    },
    setToken(token: string | null) {
      this.token = token
    },
    setUser(user: any | null) {
      this.user = user
    },
    logout() {
      this.isLoggedIn = false
      this.token = null
      this.user = null
      // localStorage에서 토큰 제거
      localStorage.removeItem('token')
    },
    initializeAuth() {
      const token = localStorage.getItem('token');
      if (token) {
        this.token = token;
        this.isLoggedIn = true;
      } else {
        this.logout(); // 토큰이 없으면 로그아웃 상태로 확실히 처리
      }
    }
  }
})
