import request from '@/utils/request'

// 提交评价
export const submitReview = (data: {
  taskId: string
  rating: number
  content?: string
  tags?: string[]
  isAnonymous?: boolean
}) => {
  return request.post('/review/submit', data)
}

// 获取评价列表
export const getReviewList = (params: {
  taskId?: string
  userId?: string
  reviewType?: number
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/review/list', { params })
}

// 获取用户评分统计
export const getUserRatingStats = (userId: string) => {
  return request.get(`/review/stats/${userId}`)
}
