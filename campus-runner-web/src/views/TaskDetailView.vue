<template>
  <div class="detail-container">
    <div class="detail-actions">
      <el-button
        :type="isFavorite ? 'danger' : 'default'"
        :icon="isFavorite ? StarFilled : Star"
        circle
        @click="handleToggleFavorite"
        :loading="favoriteLoading"
      />
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="task">
        <div class="detail-header">
          <h3>{{ task.title }}</h3>
          <span class="reward">¥{{ task.reward }}</span>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务分类">{{ task.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="取件地址">{{ task.pickupAddress }}</el-descriptions-item>
          <el-descriptions-item label="送达地址">{{ task.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="物品重量">{{ task.weight || 0 }} kg</el-descriptions-item>
          <el-descriptions-item label="期望完成时间">{{ task.expectCompleteTime }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ task.contactName }} {{ task.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ task.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>

        <div class="actions">
          <!-- 发布者视角 -->
          <template v-if="isPublisher">
            <!-- 待接单状态：可以取消发布或修改任务 -->
            <template v-if="task.status === 0">
              <el-button
                type="danger"
                size="large"
                plain
                @click="handleCancelPublish"
              >
                取消发布
              </el-button>
              <el-button
                type="primary"
                size="large"
                @click="handleEditTask"
              >
                修改任务
              </el-button>
            </template>

            <!-- 进行中状态：只能查看，不能操作（由跑腿员操作） -->
            <template v-if="task.status === 1">
              <el-button
                type="info"
                size="large"
                disabled
              >
                任务进行中
              </el-button>
            </template>

            <!-- 已完成状态：支付赏金（如未支付）和评价 -->
            <template v-if="task.status === 2">
              <el-button
                v-if="!task.isPaid"
                type="primary"
                size="large"
                @click="handlePay"
              >
                支付赏金
              </el-button>
              <el-button
                v-if="!task.isReviewed"
                type="success"
                size="large"
                @click="showReviewDialog = true"
              >
                评价跑腿员
              </el-button>
            </template>
          </template>

          <!-- 跑腿员视角 -->
          <template v-if="isRunner">
            <!-- 待接单或已取消状态：显示接单按钮 -->
            <el-button
              v-if="task.status === 0 || task.status === 3"
              type="primary"
              size="large"
              @click="handleAccept"
            >
              {{ task.status === 3 ? '重新接单' : '立即接单' }}
            </el-button>

            <!-- 进行中状态：显示取件、完成和取消按钮 -->
            <template v-if="task.status === 1">
              <el-button
                type="warning"
                size="large"
                @click="handlePickup"
              >
                确认取件
              </el-button>
              <el-button
                type="success"
                size="large"
                @click="handleDeliver"
              >
                确认送达
              </el-button>
              <el-button
                type="danger"
                size="large"
                plain
                @click="handleCancel"
              >
                取消任务
              </el-button>
            </template>

            <!-- 已完成状态：评价发布者 -->
            <template v-if="task.status === 2">
              <el-button
                v-if="!task.isReviewed"
                type="success"
                size="large"
                @click="showReviewDialog = true"
              >
                评价发布者
              </el-button>
            </template>
          </template>

          <!-- 其他用户视角（从任务大厅进入） -->
          <template v-if="!isPublisher && !isRunner">
            <!-- 待接单或已取消状态：显示接单按钮 -->
            <el-button
              v-if="task.status === 0 || task.status === 3"
              type="primary"
              size="large"
              @click="handleAccept"
            >
              {{ task.status === 3 ? '重新接单' : '立即接单' }}
            </el-button>

            <!-- 其他状态：仅显示状态信息 -->
            <el-button
              v-if="task.status === 1"
              type="info"
              size="large"
              disabled
            >
              任务进行中
            </el-button>

            <el-button
              v-if="task.status === 2"
              type="success"
              size="large"
              disabled
            >
              任务已完成
            </el-button>
          </template>
        </div>

        <!-- 评价列表 -->
        <div v-if="reviews.length > 0" class="review-section">
          <h4>任务评价</h4>
          <div v-for="review in reviews" :key="review.reviewId" class="review-item">
            <div class="review-header">
              <span class="reviewer">{{ review.reviewer?.nickname || '匿名用户' }}</span>
              <el-rate :model-value="review.rating" disabled show-score />
              <span class="review-time">{{ review.createTime }}</span>
            </div>
            <p class="review-content">{{ review.content || '暂无评价内容' }}</p>
            <div v-if="review.tags && review.tags.length > 0" class="review-tags">
              <el-tag v-for="tag in review.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 评价对话框 -->
    <el-dialog v-model="showReviewDialog" title="评价任务" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-score />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容..."
          />
        </el-form-item>
        <el-form-item label="评价标签">
          <el-checkbox-group v-model="reviewForm.tags">
            <el-checkbox label="服务周到" />
            <el-checkbox label="速度快" />
            <el-checkbox label="态度好" />
            <el-checkbox label="准时送达" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview" :loading="reviewLoading">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 编辑任务对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑任务" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="任务标题">
          <el-input v-model="editForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="任务分类">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="取件地址">
          <el-input v-model="editForm.pickupAddress" placeholder="请输入取件地址" />
        </el-form-item>
        <el-form-item label="送达地址">
          <el-input v-model="editForm.deliveryAddress" placeholder="请输入送达地址" />
        </el-form-item>
        <el-form-item label="酬金">
          <el-input-number v-model="editForm.reward" :min="1" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="物品重量(kg)">
          <el-input-number v-model="editForm.weight" :min="0" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="期望完成时间">
          <el-date-picker
            v-model="editForm.expectCompleteTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remarks" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="editLoading">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getTaskDetail, acceptTask, updateTaskStatus, updateTask, getCategoryList } from '@/api/task'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { createPayment, pay } from '@/api/payment'
import { submitReview, getReviewList } from '@/api/review'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const task = ref<any>(null)
const isFavorite = ref(false)
const favoriteLoading = ref(false)
const reviews = ref<any[]>([])
const showReviewDialog = ref(false)
const reviewLoading = ref(false)
const currentUserId = ref('')

const reviewForm = ref({
  rating: 5,
  content: '',
  tags: [] as string[],
  isAnonymous: false
})

// 编辑任务相关
const showEditDialog = ref(false)
const editLoading = ref(false)
const categories = ref<any[]>([])
const editForm = ref({
  title: '',
  description: '',
  categoryId: '',
  pickupAddress: '',
  deliveryAddress: '',
  reward: 0,
  weight: 0,
  contactName: '',
  contactPhone: '',
  expectCompleteTime: '',
  remarks: ''
})

// 计算属性：判断当前用户是否是发布者
const isPublisher = computed(() => {
  return currentUserId.value && task.value?.publisherId === currentUserId.value
})

// 计算属性：判断当前用户是否是跑腿员
const isRunner = computed(() => {
  return currentUserId.value && task.value?.runnerId === currentUserId.value
})

onMounted(() => {
  loadTaskDetail()
})

const loadTaskDetail = async () => {
  loading.value = true
  try {
    const res: any = await getTaskDetail(route.params.id as string)
    task.value = res.result
    // 获取当前用户ID
    currentUserId.value = userStore.userInfo?.id || ''
    // 加载收藏状态
    checkFavoriteStatus()
    // 加载评价列表
    loadReviews()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const checkFavoriteStatus = async () => {
  try {
    const res: any = await checkFavorite(route.params.id as string)
    isFavorite.value = res.result?.isFavorite || false
  } catch (error) {
    console.error(error)
  }
}

const handleToggleFavorite = async () => {
  favoriteLoading.value = true
  try {
    const res: any = await toggleFavorite(route.params.id as string)
    isFavorite.value = res.result?.isFavorite || false
    ElMessage.success(isFavorite.value ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    console.error(error)
  } finally {
    favoriteLoading.value = false
  }
}

const loadReviews = async () => {
  try {
    const res: any = await getReviewList({ taskId: route.params.id as string })
    reviews.value = res.result?.list || []
  } catch (error) {
    console.error(error)
  }
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该任务的赏金吗？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 创建支付订单
    const createRes: any = await createPayment({
      taskId: task.value.id,
      payeeId: task.value.runnerId,
      amount: task.value.reward,
      paymentType: 1 // 任务赏金
    })

    // 执行支付
    await pay(createRes.result.id)
    ElMessage.success('支付成功')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSubmitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  reviewLoading.value = true
  try {
    await submitReview({
      taskId: task.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      tags: reviewForm.value.tags,
      isAnonymous: reviewForm.value.isAnonymous
    })
    ElMessage.success('评价成功')
    showReviewDialog.value = false
    // 重置表单
    reviewForm.value = {
      rating: 5,
      content: '',
      tags: [],
      isAnonymous: false
    }
    loadReviews()
    loadTaskDetail()
  } catch (error) {
    console.error(error)
  } finally {
    reviewLoading.value = false
  }
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待接单', 1: '进行中', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}

const handleAccept = async () => {
  try {
    await acceptTask(task.value.id)
    ElMessage.success('接单成功')
    loadTaskDetail()
  } catch (error) {
    console.error(error)
  }
}

const handlePickup = async () => {
  try {
    await updateTaskStatus(task.value.id, { action: 'pickup' })
    ElMessage.success('取件成功')
    loadTaskDetail()
  } catch (error) {
    console.error(error)
  }
}

const handleDeliver = async () => {
  try {
    await updateTaskStatus(task.value.id, { action: 'deliver' })
    ElMessage.success('任务完成')
    loadTaskDetail()
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消任务', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '请输入取消原因...',
      inputValidator: (value: string) => {
        if (!value || value.trim() === '') {
          return '取消原因不能为空'
        }
        return true
      }
    })

    await updateTaskStatus(task.value.id, {
      action: 'cancel',
      cancelReason: value
    })
    ElMessage.success('任务已取消')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 取消发布（发布者取消待接单的任务）
const handleCancelPublish = async () => {
  try {
    await ElMessageBox.confirm('确定取消发布该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateTaskStatus(task.value.id, {
      action: 'cancel',
      cancelReason: '发布者取消发布'
    })
    ElMessage.success('任务已取消发布')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 修改任务（打开编辑对话框）
const handleEditTask = async () => {
  // 加载分类列表
  if (categories.value.length === 0) {
    try {
      const res: any = await getCategoryList()
      categories.value = res.result || []
    } catch (error) {
      console.error('加载分类失败', error)
    }
  }
  // 填充表单数据
  editForm.value = {
    title: task.value.title || '',
    description: task.value.description || '',
    categoryId: task.value.categoryId || '',
    pickupAddress: task.value.pickupAddress || '',
    deliveryAddress: task.value.deliveryAddress || '',
    reward: task.value.reward || 0,
    weight: task.value.weight || 0,
    contactName: task.value.contactName || '',
    contactPhone: task.value.contactPhone || '',
    expectCompleteTime: task.value.expectCompleteTime || '',
    remarks: task.value.remarks || ''
  }
  showEditDialog.value = true
}

// 格式化日期时间为 yyyy-MM-dd HH:mm:ss
const formatDateTime = (dateTimeStr: string | Date | null): string | undefined => {
  if (!dateTimeStr) return undefined
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!editForm.value.title) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!editForm.value.pickupAddress || !editForm.value.deliveryAddress) {
    ElMessage.warning('请输入取件地址和送达地址')
    return
  }
  
  editLoading.value = true
  try {
    await updateTask(task.value.id, {
      title: editForm.value.title,
      description: editForm.value.description,
      pickupAddress: editForm.value.pickupAddress,
      deliveryAddress: editForm.value.deliveryAddress,
      reward: editForm.value.reward,
      weight: editForm.value.weight,
      contactName: editForm.value.contactName,
      contactPhone: editForm.value.contactPhone,
      expectCompleteTime: formatDateTime(editForm.value.expectCompleteTime),
      remarks: editForm.value.remarks
    })
    ElMessage.success('任务修改成功')
    showEditDialog.value = false
    loadTaskDetail()
  } catch (error) {
    console.error(error)
    ElMessage.error('修改失败')
  } finally {
    editLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.detail-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.detail-card {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 20px;
  }
}

.reward {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 600;
}

.actions {
  margin-top: 24px;
  text-align: center;

  .el-button {
    width: 200px;
    margin: 0 8px 8px;
  }
}

.review-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;

  h4 {
    margin: 0 0 16px;
    font-size: 16px;
    color: #303133;
  }

  .review-item {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 12px;

    .review-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .reviewer {
        font-weight: 500;
        color: #303133;
      }

      .review-time {
        margin-left: auto;
        color: #909399;
        font-size: 12px;
      }
    }

    .review-content {
      margin: 8px 0;
      color: #606266;
      line-height: 1.6;
    }

    .review-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 8px;
    }
  }
}
</style>
