import request from '@/utils/request'

// 获取任务分类列表
export const getCategoryList = () => {
  return request.get('/category/list')
}

// 获取任务列表
export const getTaskList = (params: {
  pageNum?: number
  pageSize?: number
  categoryId?: string
  status?: number
  sortBy?: string
  sortOrder?: string
  keyword?: string
  weightLevel?: string
  minReward?: number
  maxReward?: number
}) => {
  return request.get('/task/list', { params })
}

// 获取任务详情
export const getTaskDetail = (taskId: string) => {
  return request.get(`/task/detail/${taskId}`)
}

// 发布任务
export const publishTask = (data: {
  categoryId: string
  title: string
  description?: string
  pickupAddress: string
  pickupLongitude?: number
  pickupLatitude?: number
  deliveryAddress: string
  deliveryLongitude?: number
  deliveryLatitude?: number
  reward: number
  weight?: number
  expectCompleteTime?: string
  contactName?: string
  contactPhone?: string
  remarks?: string
}) => {
  return request.post('/task/publish', data)
}

// 接单
export const acceptTask = (taskId: string) => {
  return request.post(`/task/accept/${taskId}`)
}

// 更新任务状态
export const updateTaskStatus = (taskId: string, data: {
  action: 'pickup' | 'deliver' | 'cancel' | 'return' | 'confirm' | 'cancelPublish' | 'publish'
  cancelReason?: string
}) => {
  return request.put(`/task/status/${taskId}`, data)
}

// 获取我的任务列表
export const getMyTasks = (params: {
  type: 'published' | 'accepted'
  status?: number
  pageNum?: number
  pageSize?: number
}) => {
  // 将type转换为后端需要的roleType参数
  const roleType = params.type === 'published' ? 'publisher' : 'runner'
  return request.get('/task/my-list', { 
    params: {
      roleType,
      status: params.status,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    }
  })
}

// 获取智能推荐任务
export const getRecommendedTasks = (params: {
  longitude: number
  latitude: number
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/task/recommend', { params })
}

// 更新任务信息(发布者修改任务)
export const updateTask = (taskId: string, data: {
  title?: string
  description?: string
  pickupAddress?: string
  deliveryAddress?: string
  reward?: number
  weight?: number
  weightLevel?: string
  expectCompleteTime?: string
  contactName?: string
  contactPhone?: string
  remarks?: string
}) => {
  return request.put(`/task/update/${taskId}`, data)
}
