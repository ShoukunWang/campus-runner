import request from '@/utils/request'

// 创建订单
export const createOrder = (data: { taskId: string }) => {
  return request.post('/order/create', data)
}

// 支付订单
export const payOrder = (orderId: string) => {
  return request.post(`/order/pay/${orderId}`)
}

// 取消订单
export const cancelOrder = (orderId: string, data: { reason: string }) => {
  return request.post(`/order/cancel/${orderId}`, data)
}

// 退款订单
export const refundOrder = (orderId: string, data: { reason: string }) => {
  return request.post(`/order/refund/${orderId}`, data)
}

// 查询我的订单
export const getMyOrders = (params: {
  status?: number
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/order/list', { params })
}

// 根据任务ID查询订单
export const getOrderByTask = (taskId: string) => {
  return request.get(`/order/task/${taskId}`)
}

// 订单统计
export const getOrderStatistics = () => {
  return request.get('/order/statistics')
}

