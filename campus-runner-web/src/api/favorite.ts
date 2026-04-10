import request from '@/utils/request'

// 切换收藏状态（收藏/取消收藏）
export const toggleFavorite = (taskId: string) => {
  return request.post('/favorite/toggle', { taskId })
}

// 获取我的收藏列表
export const getFavoriteList = (params: {
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/favorite/list', { params })
}

// 检查是否已收藏
export const checkFavorite = (taskId: string) => {
  return request.get(`/favorite/check/${taskId}`)
}
