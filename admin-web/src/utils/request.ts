import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // Assuming backend returns standard format: { code: 200, message: '...', data: ... }
    // If not wrapped, return directly. Check structure.
    if (res && typeof res.code !== 'undefined') {
      if (res.code !== 200) {
        ElMessage.error(res.message || 'Error')
        if (res.code === 401 || res.code === 403) {
          localStorage.removeItem('admin_token')
          window.location.href = '/login'
        }
        return Promise.reject(new Error(res.message || 'Error'))
      } else {
        return res.data
      }
    }
    // Fallback if not standard ApiResponse
    return res
  },
  (error) => {
    ElMessage.error(error.message || 'Request Error')
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('admin_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default service
