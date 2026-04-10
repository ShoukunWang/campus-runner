<template>
  <div class="payments-container">
    <!-- 统计卡片 -->
    <div v-if="statistics" class="statistics">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">¥{{ statistics.totalExpenseAmount || 0 }}</div>
            <div class="stat-label">累计支出</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ statistics.totalExpenseCount || 0 }}</div>
            <div class="stat-label">支出笔数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">¥{{ statistics.totalIncomeAmount || 0 }}</div>
            <div class="stat-label">累计收入</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ statistics.totalIncomeCount || 0 }}</div>
            <div class="stat-label">收入笔数</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-section">
      <el-radio-group v-model="statusFilter" @change="handleStatusChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="0">待支付</el-radio-button>
        <el-radio-button :label="1">已支付</el-radio-button>
        <el-radio-button :label="2">已退款</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else>
      <div v-if="payments.length > 0" class="payments-list">
        <el-card
          v-for="payment in payments"
          :key="payment.id"
          class="payment-card"
          shadow="hover"
        >
          <div class="payment-header">
            <span class="payment-no">订单号: {{ payment.paymentNo }}</span>
            <el-tag :type="getStatusType(payment.status)" size="small">
              {{ getStatusText(payment.status) }}
            </el-tag>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="支付类型">
              {{ getPaymentTypeText(payment.paymentType) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付金额">
              <span class="amount">¥{{ payment.amount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ payment.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="payment.payTime">
              {{ payment.payTime }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="payment.status === 0" class="payment-actions">
            <el-button type="primary" @click="handlePay(payment.id)">立即支付</el-button>
          </div>

          <div v-if="payment.status === 1 && payment.canRefund" class="payment-actions">
            <el-button type="danger" plain @click="handleRefund(payment.id)">申请退款</el-button>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无支付记录" />

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getPaymentList, getPaymentStatistics, pay, refund } from '@/api/payment'

const loading = ref(false)
const payments = ref<any[]>([])
const statistics = ref<any>(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref<number | ''>('')

onMounted(() => {
  loadStatistics()
  loadPayments()
})

const loadStatistics = async () => {
  try {
    const res: any = await getPaymentStatistics()
    statistics.value = res.result
  } catch (error) {
    console.error(error)
  }
}

const loadPayments = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res: any = await getPaymentList(params)
    payments.value = res.result?.records || []
    total.value = res.result?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已退款' }
  return texts[status] || '未知'
}

const getPaymentTypeText = (type: number) => {
  const texts: Record<number, string> = { 1: '任务赏金', 2: '违约金', 3: '其他' }
  return texts[type] || '其他'
}

const handleStatusChange = () => {
  pageNum.value = 1
  loadPayments()
}

const handlePay = async (paymentId: string) => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await pay(paymentId)
    ElMessage.success('支付成功')
    loadPayments()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleRefund = async (paymentId: string) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入退款原因...',
      inputValidator: (value: string) => {
        if (!value || value.trim() === '') {
          return '退款原因不能为空'
        }
        return true
      }
    })
    await refund(paymentId, value)
    ElMessage.success('退款申请已提交')
    loadPayments()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  pageNum.value = 1
  loadPayments()
}

const handlePageChange = (val: number) => {
  pageNum.value = val
  loadPayments()
}
</script>

<style scoped lang="scss">
.payments-container {
  max-width: 1200px;
  margin: 0 auto;
}

.statistics {
  margin-bottom: 24px;

  .stat-card {
    text-align: center;
    padding: 16px;

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #409eff;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #606266;
    }
  }
}

.filter-section {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.payments-list {
  display: grid;
  gap: 16px;
}

.payment-card {
  .payment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .payment-no {
      font-size: 14px;
      color: #606266;
    }
  }

  .amount {
    color: #f56c6c;
    font-weight: 600;
  }

  .payment-actions {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    text-align: center;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
