<template>
  <div class="order-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <template v-if="order">
        <div class="detail-header">
          <h3>订单详情</h3>
          <el-tag :type="getStatusType(order.status)" size="large">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="关联任务">
            <el-link type="primary" @click="goToTaskDetail">
              {{ order.taskNo }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额" span="2">
            <span class="amount">¥{{ order.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="付款人">
            {{ order.publisherId === userStore.userInfo?.id ? '我（发布者）' : '对方用户' }}
          </el-descriptions-item>
          <el-descriptions-item label="收款人">
            {{ order.runnerId === userStore.userInfo?.id ? '我（跑腿员）' : '对方用户' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime ? formatDateTime(order.payTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" v-if="order.refundReason">
            {{ order.refundReason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="actions">
          <!-- 发布者操作 -->
          <template v-if="order.publisherId === userStore.userInfo?.id">
            <!-- 待支付 -->
            <el-button
              v-if="order.status === 0"
              type="primary"
              size="large"
              @click="handlePay"
            >
              立即支付
            </el-button>

            <!-- 已支付：申请退款 -->
            <el-button
              v-if="order.status === 1"
              type="danger"
              size="large"
              @click="handleApplyRefund"
            >
              申请退款
            </el-button>

            <!-- 退款中 -->
            <el-button
              v-if="order.status === 2"
              type="info"
              size="large"
              disabled
            >
              退款审核中，请等待跑腿员确认
            </el-button>

            <!-- 已支付未评价：评价 -->
            <el-button
              v-if="(order.status === 1 || order.status === 4) && !order.isReviewed"
              type="success"
              size="large"
              @click="showReviewDialog = true"
            >
              评价跑腿员
            </el-button>
          </template>

          <!-- 跑腿员操作 -->
          <template v-if="order.runnerId === userStore.userInfo?.id">
            <!-- 退款中：审核 -->
            <template v-if="order.status === 2">
              <el-button
                type="success"
                size="large"
                @click="handleAuditRefund(true)"
              >
                同意退款
              </el-button>
              <el-button
                type="danger"
                size="large"
                @click="handleAuditRefund(false)"
              >
                拒绝退款
              </el-button>
            </template>

            <!-- 已支付未评价：评价 -->
            <el-button
              v-if="order.status === 1 && !order.isReviewed"
              type="success"
              size="large"
              @click="showReviewDialog = true"
            >
              评价发布者
            </el-button>
          </template>
        </div>
      </template>
    </el-card>

    <!-- 申请退款对话框 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="500px">
      <el-form :model="refundForm">
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="showReviewDialog" title="评价" width="500px">
      <div class="review-content">
        <div class="rating-section">
          <span class="label">服务评分：</span>
          <el-rate v-model="reviewForm.rating" show-score text-color="#ff9900" />
        </div>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="写下您的评价..."
          />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getOrderDetail, payOrder, applyRefund, auditRefund } from '@/api/order'
import { submitReview as submitReviewApi } from '@/api/review'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const orderId = route.params.id as string

const loading = ref(false)
const order = ref<any>(null)

const refundDialogVisible = ref(false)
const refundForm = ref({ reason: '' })

const showReviewDialog = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const res: any = await getOrderDetail(orderId)
    order.value = res.result
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '退款中',
    3: '已退款',
    4: '支付逾期'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'warning',
    3: 'info',
    4: 'danger'
  }
  return map[status] || ''
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const goToTaskDetail = () => {
  router.push(`/task/${order.value.taskId}`)
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '支付失败')
    }
  }
}

const handleApplyRefund = () => {
  refundForm.value = { reason: '' }
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.value.reason) {
    ElMessage.warning('请填写退款原因')
    return
  }
  try {
    await applyRefund(order.value.id, { reason: refundForm.value.reason })
    ElMessage.success('退款申请已提交，请等待跑腿员审核')
    refundDialogVisible.value = false
    loadOrderDetail()
  } catch (error: any) {
    ElMessage.error(error.message || '申请失败')
  }
}

const handleAuditRefund = async (agreed: boolean) => {
  try {
    const title = agreed ? '同意退款' : '拒绝退款'
    await ElMessageBox.confirm(
      agreed ? '确认同意该退款申请？' : '确认拒绝该退款申请？',
      title,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await auditRefund(order.value.id, { agreed })
    ElMessage.success(agreed ? '已同意退款' : '已拒绝退款')
    loadOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const submitReview = async () => {
  try {
    await submitReviewApi({
      taskId: order.value.taskId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      tags: [],
      isAnonymous: false
    })
    ElMessage.success('评价提交成功')
    showReviewDialog.value = false
    order.value.isReviewed = true
  } catch (error: any) {
    ElMessage.error(error.message || '评价提交失败')
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped lang="scss">
.order-detail-container {
  padding: 20px;

  .detail-card {
    max-width: 800px;
    margin: 0 auto;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
    }

    .amount {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-color-danger);
    }

    .actions {
      margin-top: 32px;
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }

  .review-content {
    .rating-section {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .label {
        margin-right: 12px;
      }
    }
  }
}
</style>
