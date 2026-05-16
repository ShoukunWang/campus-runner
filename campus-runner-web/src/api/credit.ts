import request from '@/utils/request'

export const getMyCreditRecords = (params: { pageNum: number; pageSize: number }) => {
  return request.get('/credit/my-records', { params })
}

export const getRestorePrice = (targetScore: number) => {
  return request.get('/credit/restore-price', { params: { targetScore } })
}

export const restoreCredit = (data: { targetScore: number; paidAmount: number }) => {
  return request.post('/credit/restore', data)
}

export const getMyCreditScore = () => {
  return request.get('/credit/my-score')
}