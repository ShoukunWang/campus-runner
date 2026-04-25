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
          <el-descriptions-item label="物品重量">{{ getWeightLevel(task.weight) }}</el-descriptions-item>
          <el-descriptions-item label="期望完成时间">{{ task.expectCompleteTime }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ task.contactName }} {{ task.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ task.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 任务进度条 -->
        <div class="task-progress" v-if="task.status !== 3">
          <el-steps :active="getCurrentStep()" align-center>
            <el-step title="待接取" description="等待跑腿员接单" />
            <el-step title="已接取" description="跑腿员已接单" />
            <el-step title="已取件" description="跑腿员已取件" />
            <el-step title="已完成" description="任务已完成" />
          </el-steps>
        </div>

        <div class="actions">
          <!-- 发布者视角 -->
          <template v-if="isPublisher">
            <!-- 草稿/待发布状态：发布或继续修改 -->
            <template v-if="task.status === -1 || task.status === 5">
              <el-button
                type="primary"
                size="large"
                @click="handlePublish"
              >
                发布任务
              </el-button>
              <el-button
                type="warning"
                size="large"
                @click="handleEditTask"
              >
                修改任务
              </el-button>
            </template>

            <!-- 待接取状态：取消发布回到草稿 或 修改任务 -->
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

            <!-- 进行中状态：跑腿员已接单，等待处理 -->
            <template v-if="task.status === 1">
              <el-button
                type="info"
                size="large"
                disabled
              >
                已接单，待跑腿员提货
              </el-button>
            </template>

            <!-- 已提货状态：跑腿员配送中 -->
            <template v-if="task.status === 2">
              <el-button
                type="primary"
                size="large"
                disabled
              >
                跑腿员已提货，配送中
              </el-button>
            </template>

            <!-- 待确认状态：发布者确认收到物品 -->
            <template v-if="task.status === 3">
              <el-button
                type="success"
                size="large"
                @click="handleConfirmReceive"
              >
                确认收到
              </el-button>
              <el-button
                type="info"
                size="large"
                disabled
              >
                3天内未确认将自动完成
              </el-button>
            </template>

            <!-- 已完成状态：跳转到订单处理支付和评价 -->
            <template v-if="task.status === 4">
              <el-button
                type="primary"
                size="large"
                @click="goToOrderDetail"
              >
                查看订单
              </el-button>
            </template>
          </template>

          <!-- 跑腿员视角 -->
          <template v-if="isRunner">
            <!-- 待接取或已退回状态：显示接单按钮 -->
            <el-button
              v-if="task.status === 0 || task.status === 6"
              type="primary"
              size="large"
              @click="handleAccept"
            >
              {{ task.status === 6 ? '重新接单' : '立即接单' }}
            </el-button>

            <!-- 进行中状态：显示提货按钮 -->
            <template v-if="task.status === 1 && task.runnerId === userStore.userInfo?.id">
              <el-button
                type="warning"
                size="large"
                @click="handlePickup"
              >
                确认提货
              </el-button>
              <el-button
                type="danger"
                size="large"
                plain
                @click="handleCancel"
              >
                取消接单
              </el-button>
            </template>

            <!-- 已提货状态：显示送达按钮 -->
            <template v-if="task.status === 2 && task.runnerId === userStore.userInfo?.id">
              <el-button
                type="success"
                size="large"
                @click="handleDeliver"
              >
                确认送达
              </el-button>
            </template>

            <!-- 待确认状态：等待发布者确认 -->
            <template v-if="task.status === 3 && task.runnerId === userStore.userInfo?.id">
              <el-button
                type="info"
                size="large"
                disabled
              >
                等待发布者确认
              </el-button>
            </template>

            <!-- 已完成状态：评价发布者 -->
            <template v-if="task.status === 4 && task.runnerId === userStore.userInfo?.id">
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
            <!-- 待接取或已退回状态：显示接单按钮 -->
            <el-button
              v-if="task.status === 0 || task.status === 6"
              type="primary"
              size="large"
              @click="handleAccept"
            >
              {{ task.status === 6 ? '重新接单' : '立即接单' }}
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
              v-if="task.status === 4"
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
        <el-form-item label="物品重量">
          <el-select v-model="editForm.weightLevel" placeholder="请选择物品重量等级" style="width: 100%">
            <el-option label="🎒 小件 (0-2kg) - 文件、小包裹、零食等" value="small" />
            <el-option label="📦 中件 (2-5kg) - 书本、中型包裹、日用品等" value="medium" />
            <el-option label="📦 大件 (5-10kg) - 行李箱、大包裹、成箱物品等" value="large" />
            <el-option label="🚚 超大件 (10kg+) - 家具、大型设备、批量物品等" value="extra_large" />
          </el-select>
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
import { getOrderByTask, payOrder } from '@/api/order'
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
  weightLevel: '',
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
    await checkFavoriteStatus()
    // 加载评价列表
    await loadReviews()
    // 加载订单状态，判断是否已支付
    await loadOrderStatus()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadOrderStatus = async () => {
  try {
    // 只有已完成(status=4)的任务才有订单
    if (task.value.status === 4) {
      const res: any = await getOrderByTask(task.value.id)
      if (res.result) {
        // 订单状态：0-待支付，1-已支付，2-已取消，3-已退款
        task.value.isPaid = res.result.status === 1
      } else {
        // 订单不存在，可能是任务刚完成，订单还在创建中
        // 等待1秒后重试一次
        console.log('订单还未创建，等待重试...')
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const retryRes: any = await getOrderByTask(task.value.id)
        if (retryRes.result) {
          task.value.isPaid = retryRes.result.status === 1
        } else {
          task.value.isPaid = false
        }
      }
    }
  } catch (error) {
    // 订单查询失败不影响页面展示
    console.warn('加载订单状态失败，设置为未支付', error)
    task.value.isPaid = false
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

    // 查询订单（任务完成后自动创建）
    const orderRes: any = await getOrderByTask(task.value.id)
    
    if (!orderRes.result) {
      ElMessage.warning('订单还未创建，请稍后再试')
      return
    }

    // 执行支付
    await payOrder(orderRes.result.id)
    ElMessage.success('支付成功')
    
    // 重新加载任务详情和订单状态
    await loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '支付失败')
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
  const map: Record<number, string> = {
    [-1]: 'info',
    0: 'warning',
    1: 'primary',
    2: 'primary',
    3: 'warning',
    4: 'success',
    5: 'danger',
    6: 'info'
  }
  return map[status] || ''
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    [-1]: '待发布',
    0: '待接取',
    1: '进行中',
    2: '已提货',
    3: '待确认',
    4: '已完成',
    5: '已取消',
    6: '已退回'
  }
  return map[status] || '未知'
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
    ElMessage.success('已确认送达，请等待发布者确认收到')
    loadTaskDetail()
  } catch (error) {
    console.error(error)
  }
}

// 发布者确认收到物品
const handleConfirmReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到物品吗？确认后将创建待支付订单', '提示', {
      confirmButtonText: '确认收到',
      cancelButtonText: '取消',
      type: 'success'
    })
    await updateTaskStatus(task.value.id, { action: 'confirm' })
    ElMessage.success('确认成功，订单已创建，请及时支付赏金')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCancel = async () => {
  try {
    // 判断是发布者还是跑腿员
    const isRunnerCancel = isRunner.value
    
    const { value } = await ElMessageBox.prompt(
      isRunnerCancel ? '请输入退回原因' : '请输入取消原因', 
      isRunnerCancel ? '退回任务' : '取消任务',
      {
        confirmButtonText: '确定',
        cancelButtonText: '返回',
        inputPlaceholder: isRunnerCancel ? '请输入退回原因...' : '请输入取消原因...',
        inputValidator: (value: string) => {
          if (!value || value.trim() === '') {
            return isRunnerCancel ? '退回原因不能为空' : '取消原因不能为空'
          }
          return true
        }
      }
    )

    // 跑腿员退回任务使用 action: 'return'，发布者取消使用 action: 'cancel'
    const action = isRunnerCancel ? 'return' : 'cancel'
    
    await updateTaskStatus(task.value.id, {
      action: action,
      cancelReason: value
    })
    
    ElMessage.success(isRunnerCancel ? '任务已退回' : '任务已取消')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消/退回任务失败:', error)
    }
  }
}

// 取消发布（发布者取消待接单的任务）
const handleCancelPublish = async () => {
  try {
    await ElMessageBox.confirm('确定取消发布该任务吗？将回到草稿状态，可重新修改发布。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateTaskStatus(task.value.id, {
      action: 'cancelPublish',
      cancelReason: '发布者取消发布'
    })
    ElMessage.success('任务已取消发布，已转为草稿状态')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handlePublish = async () => {
  try {
    await ElMessageBox.confirm('确定发布该任务吗？发布后将在任务大厅显示。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateTaskStatus(task.value.id, {
      action: 'publish',
      cancelReason: ''
    })
    ElMessage.success('任务发布成功')
    loadTaskDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 跳转到订单详情
const goToOrderDetail = async () => {
  try {
    const res: any = await getOrderByTask(task.value.id)
    if (res.result) {
      router.push(`/order/${res.result.id}`)
    } else {
      ElMessage.error('订单数据尚未生成，请稍后再试')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取订单信息失败')
  }
}

// 重量等级转换
const getWeightLevel = (weight: number) => {
  if (!weight && weight !== 0) return '未指定'
  if (weight <= 2) return '🎒 小件 (0-2kg)'
  if (weight <= 5) return '📦 中件 (2-5kg)'
  if (weight <= 10) return '📦 大件 (5-10kg)'
  return '🚚 超大件 (10kg+)'
}

// 获取当前任务进度步骤
const getCurrentStep = () => {
  if (!task.value) return 0
  // status=0: 待接取 (step 0)
  if (task.value.status === 0) return 0
  // status=1 且没有取件时间: 已接取 (step 1)
  if (task.value.status === 1 && !task.value.pickupTime) return 1
  // status=1 且有取件时间 或 status=2: 已取件/已完成 (step 2-3)
  if (task.value.status === 1 && task.value.pickupTime) return 2
  if (task.value.status === 2) return 3
  return 0
}

// 重量数值转等级key
const getWeightLevelKey = (weight: number) => {
  if (!weight && weight !== 0) return ''
  if (weight <= 2) return 'small'
  if (weight <= 5) return 'medium'
  if (weight <= 10) return 'large'
  return 'extra_large'
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
    weightLevel: getWeightLevelKey(task.value.weight),
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
    // 将重量等级转换为具体重量值
    const weightMap: Record<string, number> = {
      small: 1,
      medium: 3.5,
      large: 7.5,
      extra_large: 12
    }
    
    await updateTask(task.value.id, {
      title: editForm.value.title,
      description: editForm.value.description,
      pickupAddress: editForm.value.pickupAddress,
      deliveryAddress: editForm.value.deliveryAddress,
      reward: editForm.value.reward,
      weight: weightMap[editForm.value.weightLevel] || 1,
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

.task-progress {
  margin-top: 24px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  
  :deep(.el-step__title) {
    font-size: 14px;
    font-weight: 600;
  }
  
  :deep(.el-step__description) {
    font-size: 12px;
    color: #909399;
  }
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
