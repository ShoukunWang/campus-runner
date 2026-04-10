import request from '@/utils/request'

// 获取当前管理员信息
export const getCurrentAdmin = () => {
  return request.get('/admin/current')
}

// 获取统计数据
export const getStatistics = () => {
  return request.get('/admin/statistics')
}

// 获取用户列表
export const getUserList = (params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  role?: number
  status?: number
}) => {
  return request.get('/admin/user/list', { params })
}

// 更新用户状态
export const updateUserStatus = (userId: string, status: number) => {
  return request.put(`/admin/user/status/${userId}`, { status })
}

// 更新用户信息
export const updateUserInfo = (userId: string, data: {
  nickname?: string
  phone?: string
  email?: string
  creditScore?: number
}) => {
  return request.put(`/admin/user/${userId}`, data)
}

// 获取任务列表
export const getAdminTaskList = (params: {
  pageNum?: number
  pageSize?: number
  taskNo?: string
  title?: string
  status?: number
  publisherName?: string
  runnerName?: string
}) => {
  return request.get('/admin/task/list', { params })
}

// 更新任务
export const updateAdminTask = (taskId: string, data: {
  title?: string
  description?: string
  reward?: number
  status?: number
  cancelReason?: string
}) => {
  return request.put(`/admin/task/${taskId}`, data)
}

// 删除任务
export const deleteAdminTask = (taskId: string) => {
  return request.delete(`/admin/task/${taskId}`)
}

// ==================== 管理员管理 ====================

// 获取管理员列表
export const getAdminList = (params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
}) => {
  return request.get('/admin/list', { params })
}

// 添加管理员
export const addAdmin = (data: {
  username: string
  password: string
  realName?: string
  phone?: string
  email?: string
  role?: number
}) => {
  return request.post('/admin/add', data)
}

// 更新管理员信息
export const updateAdmin = (adminId: string, data: {
  realName?: string
  phone?: string
  email?: string
  role?: number
  status?: number
}) => {
  return request.put(`/admin/${adminId}`, data)
}
