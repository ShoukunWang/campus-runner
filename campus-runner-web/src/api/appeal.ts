import request from '@/utils/request'

export const createAppeal = (data: {
  orderId: string
  appealType: number
  evidence?: string
}) => {
  return request.post('/appeal', data)
}

export const getMyAppeals = (params: { pageNum: number; pageSize: number }) => {
  return request.get('/appeal/my', { params })
}

export const getAppealDetail = (id: string) => {
  return request.get(`/appeal/${id}`)
}

export const getAdminAppealList = (params: { pageNum: number; pageSize: number; status?: number }) => {
  return request.get('/admin/appeal/list', { params })
}

export const auditAppeal = (id: string, data: { status: number; remark?: string }) => {
  return request.post(`/admin/appeal/${id}/audit`, data)
}

export const getCompensationPool = () => {
  return request.get('/admin/credit-pool')
}