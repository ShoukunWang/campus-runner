import request from '@/utils/request'

// 创建支付订单
export const createPayment = (data: {
  taskId: string
  payeeId: string
  amount: number
  paymentType: number
}) => {
  return request.post('/payment/create', data)
}

// 执行支付
export const pay = (paymentId: string) => {
  return request.post(`/payment/pay/${paymentId}`)
}

// 申请退款
export const refund = (paymentId: string, reason?: string) => {
  return request.post(`/payment/refund/${paymentId}`, { reason })
}

// 获取我的支付记录
export const getPaymentList = (params: {
  status?: number
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/payment/list', { params })
}

// 获取支付统计
export const getPaymentStatistics = () => {
  return request.get('/payment/statistics')
}
