<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
        </div>
      </template>

      <!-- 订单统计卡片 -->
      <el-row :gutter="20" class="stats-cards" v-if="statistics">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
            <div class="stat-label">待支付</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value success">￥{{ statistics.incomeAmount || 0 }}</div>
            <div class="stat-label">总收入</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value danger">￥{{ statistics.expenseAmount || 0 }}</div>
            <div class="stat-label">总支出</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value primary">{{ statistics.completedCount || 0 }}</div>
            <div class="stat-label">已完成</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 订单状态筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="filterStatus" @change="loadOrders">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="0">待支付</el-radio-button>
          <el-radio-button :value="1">已支付</el-radio-button>
          <el-radio-button :value="2">已取消</el-radio-button>
          <el-radio-button :value="3">已退款</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 订单列表 -->
      <el-table :data="orders" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column prop="taskNo" label="任务编号" width="150" />
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="付款人" width="120">
          <template #default="{ row }">
            {{ row.publisherId === userStore.userInfo?.id ? '我' : '对方' }}
          </template>
        </el-table-column>
        <el-table-column label="收款人" width="120">
          <template #default="{ row }">
            {{ row.runnerId === userStore.userInfo?.id ? '我' : '对方' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 && row.publisherId === userStore.userInfo?.id"
              type="primary"
              size="small"
              @click="handlePay(row)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 1 && row.publisherId === userStore.userInfo?.id"
              type="danger"
              size="small"
              @click="handleRefund(row)"
            >
              退款
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewTaskDetail(row.taskId)"
            >
              查看任务
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态提示 -->
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单数据" />

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="500px">
      <el-form :model="cancelForm">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="4" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCancel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 退款订单对话框 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="500px">
      <el-form :model="refundForm">
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="4" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyOrders, payOrder, cancelOrder, refundOrder, getOrderStatistics } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const orders = ref<any[]>([])
const loading = ref(false)
const filterStatus = ref<number | undefined>(undefined)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref<any>(null)

const cancelDialogVisible = ref(false)
const cancelForm = ref({
  orderId: '',
  reason: ''
})

const refundDialogVisible = ref(false)
const refundForm = ref({
  orderId: '',
  reason: ''
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res: any = await getMyOrders({
      status: filterStatus.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    orders.value = res.result.records
    total.value = res.result.total
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res: any = await getOrderStatistics()
    statistics.value = res.result
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款'
  }
  return textMap[status] || '未知'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handlePay = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '支付失败')
    }
  }
}

const handleCancel = (order: any) => {
  cancelForm.value = {
    orderId: order.id,
    reason: ''
  }
  cancelDialogVisible.value = true
}

const submitCancel = async () => {
  if (!cancelForm.value.reason) {
    ElMessage.warning('请填写取消原因')
    return
  }
  
  try {
    await cancelOrder(cancelForm.value.orderId, { reason: cancelForm.value.reason })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch (error: any) {
    ElMessage.error(error.message || '取消失败')
  }
}

const handleRefund = (order: any) => {
  refundForm.value = {
    orderId: order.id,
    reason: ''
  }
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.value.reason) {
    ElMessage.warning('请填写退款原因')
    return
  }
  
  try {
    await refundOrder(refundForm.value.orderId, { reason: refundForm.value.reason })
    ElMessage.success('退款申请已提交')
    refundDialogVisible.value = false
    loadOrders()
  } catch (error: any) {
    ElMessage.error(error.message || '退款失败')
  }
}

const viewTaskDetail = (taskId: string) => {
  router.push(`/task/${taskId}`)
}

onMounted(() => {
  loadOrders()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.orders-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }
  
  .stats-cards {
    margin-bottom: 24px;
    
    .stat-card {
      text-align: center;
      padding: 16px 0;
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 8px;
        
        &.success {
          color: #67c23a;
        }
        
        &.danger {
          color: #f56c6c;
        }
        
        &.primary {
          color: #409eff;
        }
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .filter-bar {
    margin-bottom: 20px;
  }
  
  .amount {
    color: #f56c6c;
    font-weight: 600;
  }
}
</style>
