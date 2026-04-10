import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: Token过期或未登录
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    // 处理HTTP错误响应
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 401: 未授权（Token无效或过期）
      if (status === 401) {
        ElMessage.error(data?.message || '登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        return Promise.reject(error)
      }
      
      const message = data?.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(error)
    }
    
    // 处理网络错误（后端未启动或连接失败）
    ElMessage.error('网络错误，请检查后端服务是否启动')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    return Promise.reject(error)
  }
)

export default request
