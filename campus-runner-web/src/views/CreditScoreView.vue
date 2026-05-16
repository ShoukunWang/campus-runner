<template>
  <div class="credit-container">
    <el-card class="credit-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>信誉分详情</span>
          <el-button type="primary" link @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <div class="credit-score-section">
        <div class="score-display">
          <div class="score-circle" :class="scoreClass">
            <span class="score-value">{{ creditScore }}</span>
            <span class="score-label">信誉分</span>
          </div>
        </div>

        <div class="score-info">
          <div class="info-item">
            <span class="label">当前状态</span>
            <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">发任务权限</span>
            <el-tag :type="canPublish ? 'success' : 'danger'" size="large">
              {{ canPublish ? '可发布' : '禁止发布' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">接任务权限</span>
            <el-tag :type="canAccept ? 'success' : 'danger'" size="large">
              {{ canAccept ? '可接单' : '禁止接单' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="restore-section" v-if="creditScore < 100">
        <h4>恢复信誉分</h4>
        <div class="price-table">
          <div class="price-row header">
            <span>目标分数</span>
            <span>单价</span>
            <span>费用</span>
            <span>操作</span>
          </div>
          <div v-for="target in restoreTargets" :key="target.score" class="price-row">
            <span>{{ creditScore }} → {{ target.score }}</span>
            <span>¥{{ target.price }}/分</span>
            <span class="amount">¥{{ calculateTotalPrice(target.score, target.price) }}</span>
            <el-button
              type="primary"
              size="small"
              :disabled="isRestoring"
              @click="handleRestore(target.score)"
            >
              恢复
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="records-section">
        <h4>信誉分变动记录</h4>
        <el-table :data="records" style="width: 100%" v-loading="recordsLoading">
          <el-table-column prop="createTime" label="时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="120">
            <template #default="scope">
              <el-tag size="small" :type="getTypeTagType(scope.row.type)">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="changeAmount" label="变化" width="100">
            <template #default="scope">
              <span :class="scope.row.changeAmount > 0 ? 'positive' : 'negative'">
                {{ scope.row.changeAmount > 0 ? '+' : '' }}{{ scope.row.changeAmount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="beforeScore" label="变化前" width="80" align="center" />
          <el-table-column prop="afterScore" label="变化后" width="80" align="center" />
          <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        </el-table>

        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadRecords"
          class="table-pagination"
        />
      </div>
    </el-card>

    <el-dialog v-model="restoreDialogVisible" title="恢复信誉分" width="400px">
      <div class="restore-dialog-content">
        <p>当前信誉分：<strong>{{ creditScore }}</strong></p>
        <p>目标信誉分：<strong>{{ targetScore }}</strong></p>
        <p class="price">应付金额：<strong class="amount">¥{{ restorePrice }}</strong></p>
        <el-alert
          title="支付成功后，信誉分将立即恢复"
          type="success"
          :closable="false"
          style="margin-top: 16px"
        />
      </div>
      <template #footer>
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRestore" :loading="isRestoring">
          确认支付 ¥{{ restorePrice }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyCreditScore, getMyCreditRecords, getRestorePrice, restoreCredit } from '@/api/credit'

const userStore = useUserStore()

const loading = ref(false)
const recordsLoading = ref(false)
const isRestoring = ref(false)

const creditScore = ref(100)
const canPublish = ref(true)
const canAccept = ref(true)
const isFrozen = ref(false)

const records = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const restoreDialogVisible = ref(false)
const targetScore = ref(100)
const restorePrice = ref(0)

const restoreTargets = [
  { score: 60, price: 0.5 },
  { score: 80, price: 2 },
  { score: 90, price: 5 },
  { score: 100, price: 10 }
]

const scoreClass = computed(() => {
  if (creditScore.value >= 80) return 'score-high'
  if (creditScore.value >= 60) return 'score-medium'
  if (creditScore.value >= 40) return 'score-low'
  return 'score-critical'
})

const statusType = computed(() => {
  if (isFrozen.value) return 'danger'
  if (creditScore.value >= 80) return 'success'
  if (creditScore.value >= 60) return 'warning'
  return 'danger'
})

const statusText = computed(() => {
  if (isFrozen.value) return '账号已冻结'
  if (creditScore.value >= 80) return '优秀'
  if (creditScore.value >= 60) return '良好'
  if (creditScore.value >= 40) return '受限'
  return '严重受限'
})

const getTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: '违规扣分',
    2: '支付恢复',
    3: '自然恢复',
    4: '管理员调整'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: number) => {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'success',
    3: 'success',
    4: 'warning'
  }
  return map[type] || 'info'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const calculateTotalPrice = (target: number, unitPrice: number) => {
  const diff = target - creditScore.value
  if (diff <= 0) return 0
  return (diff * unitPrice).toFixed(2)
}

const loadCreditScore = async () => {
  loading.value = true
  try {
    const res: any = await getMyCreditScore()
    creditScore.value = res.result.creditScore
    canPublish.value = res.result.canPublish
    canAccept.value = res.result.canAccept
    isFrozen.value = res.result.isFrozen
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const res: any = await getMyCreditRecords({ pageNum: pageNum.value, pageSize: pageSize.value })
    records.value = res.result.records || []
    total.value = res.result.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    recordsLoading.value = false
  }
}

const handleRefresh = () => {
  loadCreditScore()
  loadRecords()
}

const handleRestore = async (target: number) => {
  targetScore.value = target
  try {
    const res: any = await getRestorePrice(target)
    restorePrice.value = res.result.price
    restoreDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取价格失败')
  }
}

const confirmRestore = async () => {
  isRestoring.value = true
  try {
    await restoreCredit({
      targetScore: targetScore.value,
      paidAmount: restorePrice.value
    })
    ElMessage.success('信誉分恢复成功')
    restoreDialogVisible.value = false
    loadCreditScore()
    loadRecords()
  } catch (error: any) {
    ElMessage.error(error.message || '恢复失败')
  } finally {
    isRestoring.value = false
  }
}

onMounted(() => {
  loadCreditScore()
  loadRecords()
})
</script>

<style scoped lang="scss">
.credit-container {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.credit-score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px 0;
}

.score-display {
  .score-circle {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 6px solid;

    &.score-high {
      border-color: #67c23a;
      background-color: rgba(103, 194, 58, 0.1);
      .score-value { color: #67c23a; }
    }
    &.score-medium {
      border-color: #e6a23c;
      background-color: rgba(230, 162, 60, 0.1);
      .score-value { color: #e6a23c; }
    }
    &.score-low {
      border-color: #f56c6c;
      background-color: rgba(245, 108, 108, 0.1);
      .score-value { color: #f56c6c; }
    }
    &.score-critical {
      border-color: #909399;
      background-color: rgba(144, 147, 153, 0.1);
      .score-value { color: #909399; }
    }

    .score-value {
      font-size: 36px;
      font-weight: 600;
    }
    .score-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.score-info {
  flex: 1;
  .info-item {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;

    .label {
      width: 100px;
      color: #606266;
    }
  }
}

.restore-section, .records-section {
  h4 {
    margin: 16px 0;
    font-size: 16px;
    color: #303133;
  }
}

.price-table {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;

  .price-row {
    display: grid;
    grid-template-columns: 1.5fr 1fr 1fr 100px;
    gap: 16px;
    padding: 12px 0;
    align-items: center;

    &.header {
      font-weight: 600;
      color: #606266;
      border-bottom: 1px solid #ebeef5;
    }

    &:not(.header) {
      border-bottom: 1px solid #ebeef5;
      &:last-child { border-bottom: none; }
    }

    .amount {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.positive { color: #67c23a; font-weight: 600; }
.negative { color: #f56c6c; font-weight: 600; }

.restore-dialog-content {
  p {
    margin: 8px 0;
    font-size: 16px;
    .amount { color: #f56c6c; font-size: 20px; }
  }
}
</style>