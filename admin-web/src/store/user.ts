import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    role: localStorage.getItem('admin_role') || ''
  }),
  actions: {
    setToken(token: string, role: string) {
      this.token = token
      this.role = role
      localStorage.setItem('admin_token', token)
      localStorage.setItem('admin_role', role)
    },
    logout() {
      this.token = ''
      this.role = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_role')
      window.location.href = '/login'
    }
  }
})
