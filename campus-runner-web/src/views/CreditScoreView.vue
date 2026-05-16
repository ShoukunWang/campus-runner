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
            <div>目标分数</div>
            <div>费用</div>
            <div class="action-cell">操作</div>
          </div>
          <div v-for="target in availableTargets" :key="target.score" class="price-row">
            <div>{{ creditScore }} → {{ target.score }}</div>
            <div><span class="amount">¥{{ target.price }}</span></div>
            <div class="action-cell">
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

const restoreTargets = [80, 90, 100]
const restorePrices = ref<Record<number, string>>({})

const availableTargets = computed(() => {
  return restoreTargets
    .filter(score => score > creditScore.value)
    .map(score => ({
      score,
      price: restorePrices.value[score] || '计算中...'
    }))
})

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

const loadCreditScore = async () => {
  loading.value = true
  try {
    const res: any = await getMyCreditScore()
    creditScore.value = res.result.creditScore
    canPublish.value = res.result.canPublish
    canAccept.value = res.result.canAccept
    isFrozen.value = res.result.isFrozen
    
    await loadRestorePrices()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadRestorePrices = async () => {
  for (const target of restoreTargets) {
    if (target > creditScore.value) {
      try {
        const res: any = await getRestorePrice(target)
        restorePrices.value[target] = res.result.price
      } catch (error) {
        console.error(`Failed to get price for ${target}:`, error)
      }
    }
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
  try {
    const res: any = await getRestorePrice(target)
    targetScore.value = target
    restorePrice.value = res.result.price
    restoreDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取价格失败')
  }
}

const confirmRestore = async () => {
  isRestoring.value = true
  try {
    await restoreCredit({ targetScore: targetScore.value, paidAmount: parseFloat(restorePrice.value) })
    ElMessage.success('恢复成功')
    restoreDialogVisible.value = false
    
    creditScore.value = targetScore.value
    if (userStore.userInfo) {
      userStore.userInfo.creditScore = targetScore.value
    }
    
    loadCreditScore()
    loadRecords()
  } catch (error) {
    ElMessage.error('恢复失败')
  } finally {
    isRestoring.value = false
  }
}

onMounted(() => {
  loadCreditScore()
  loadRecords()
})
</script>

<style scoped>
.credit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.credit-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.credit-score-section {
  display: flex;
  align-items: center;
  gap: 60px;
  margin-bottom: 30px;
}

.score-display {
  flex-shrink: 0;
}

.score-circle {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.score-high {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.score-medium {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
}

.score-low {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.score-critical {
  background: linear-gradient(135deg, #409eff, #73b4ff);
}

.score-value {
  font-size: 48px;
  color: #fff;
}

.score-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.score-info {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.info-item .label {
  width: 100px;
  color: #666;
}

.restore-section {
  margin-top: 30px;
}

.restore-section h4 {
  margin-bottom: 16px;
  font-size: 16px;
}

.price-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
}

.price-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.price-row:last-child {
  border-bottom: none;
}

.price-row.header {
  background: #f5f7fa;
  font-weight: bold;
  color: #666;
}

.price-row > div:nth-child(1) {
  width: 35%;
  padding-left: 16px;
}

.price-row > div:nth-child(2) {
  width: 35%;
  text-align: center;
}

.price-row > div:nth-child(3) {
  width: 30%;
  padding-right: 16px;
}

.price-row .action-cell {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.price-row .action-cell .el-button {
  width: 60px;
  margin: 0;
}

.price-row span.amount {
  color: #f56c6c;
  font-weight: bold;
}

.records-section {
  margin-top: 20px;
}

.records-section h4 {
  margin-bottom: 16px;
  font-size: 16px;
}

.table-pagination {
  text-align: right;
  margin-top: 16px;
}

.positive {
  color: #67c23a;
}

.negative {
  color: #f56c6c;
}

.restore-dialog-content p {
  margin-bottom: 12px;
}

.restore-dialog-content .price {
  font-size: 18px;
  margin-top: 16px;
}

.restore-dialog-content .amount {
  color: #f56c6c;
  font-size: 24px;
}
</style>