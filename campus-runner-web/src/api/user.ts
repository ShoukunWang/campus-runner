import request from '@/utils/request'

// 用户注册
export const register = (data: {
  username: string
  password: string
  phone: string
  nickname?: string
  role?: number
}) => {
  return request.post('/user/register', data)
}

// 用户登录
export const login = (data: {
  username: string
  password: string
}) => {
  // 后端使用 account 字段，这里做转换
  return request.post('/user/login', {
    account: data.username,
    password: data.password
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/user/current')
}

// 更新用户信息
export const updateUserInfo = (data: {
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
}) => {
  return request.put('/user/profile', data)
}

// 退出登录
export const logout = () => {
  return request.post('/user/logout')
}

// 管理员登录
export const adminLogin = (data: {
  username: string
  password: string
}) => {
  return request.post('/admin/login', data)
}
