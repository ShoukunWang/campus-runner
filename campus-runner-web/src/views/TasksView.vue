<template>
  <div class="tasks-container">
    <div class="header">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
      <h2>任务大厅</h2>
      <div></div>
    </div>
    
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索任务标题、描述或地址..."
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
        style="width: 400px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>
    
    <div class="filter-bar">
      <el-select v-model="filter.categoryId" placeholder="任务分类" clearable>
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      
      <el-select v-model="filter.sortBy" placeholder="排序方式">
        <el-option label="最新发布" value="createTime" />
        <el-option label="酬金最高" value="reward" />
      </el-select>
      
      <el-button type="primary" @click="loadTasks">筛选</el-button>
    </div>
    
    <div class="task-list" v-loading="loading">
      <el-card 
        v-for="task in tasks" 
        :key="task.id" 
        class="task-card" 
        :class="{ 'my-task': isMyTask(task.publisherId) }"
        @click="goDetail(task.id)"
      >
        <div class="task-header">
          <div class="header-left">
            <el-tag size="small">{{ getCategoryName(task.categoryId) }}</el-tag>
            <el-tag v-if="isMyTask(task.publisherId)" type="primary" size="small" class="my-tag">我的</el-tag>
          </div>
          <span class="reward">¥{{ task.reward }}</span>
        </div>
        <h4 class="task-title">{{ task.title }}</h4>
        <p class="task-address">
          <el-icon><Location /></el-icon>
          {{ task.pickupAddress }} → {{ task.deliveryAddress }}
        </p>
        <div class="task-footer">
          <span class="time">{{ formatTime(task.createTime) }}</span>
          <el-tag :type="getStatusType(task.status)" size="small">{{ getTaskProgressText(task) }}</el-tag>
        </div>
      </el-card>
    </div>
    
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @change="loadTasks"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Location, Search } from '@element-plus/icons-vue'
import { getTaskList, getCategoryList } from '@/api/task'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tasks = ref<any[]>([])
const categories = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const filter = ref({
  categoryId: '',
  sortBy: 'createTime'
})

onMounted(() => {
  loadCategories()
  loadTasks()
})

const loadCategories = async () => {
  try {
    const res: any = await getCategoryList()
    categories.value = res.result
  } catch (error) {
    console.error(error)
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res: any = await getTaskList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      categoryId: filter.value.categoryId || undefined,
      sortBy: filter.value.sortBy,
      sortOrder: 'desc',
      keyword: searchKeyword.value || undefined
    })
    tasks.value = res.result.records || res.result.list || []
    total.value = res.result.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pageNum.value = 1 // 重置到第一页
  loadTasks()
}

// 判断是否是自己发布的任务
const isMyTask = (publisherId: string) => {
  return userStore.userInfo?.id === publisherId
}

const getCategoryName = (id: string) => {
  const cat = categories.value.find(c => c.id === id)
  return cat?.name || '其他'
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'warning', 5: 'primary' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    [-1]: '待发布',
    0: '待接取',
    1: '进行中',
    2: '已提货',
    3: '待确认',
    4: '已完成',
    5: '已取消',
    6: '待接取'
  }
  return texts[status] || '未知'
}

// 获取任务进度文本
const getTaskProgressText = (task: any) => {
  if (task.status === 0 || task.status === 6) return '待接取'
  if (task.status === 1) return '已接取'
  if (task.status === 2) return '已提货'
  if (task.status === 3) return '待确认'
  if (task.status === 4) return '已完成'
  if (task.status === 5) return '已取消'
  return '未知'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleDateString()
}

const goDetail = (id: string) => {
  router.push(`/task/${id}`)
}
</script>

<style scoped lang="scss">
.tasks-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.task-card {
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
  
  &:hover {
    transform: translateY(-2px);
  }
  
  // 自己发布的任务样式
  &.my-task {
    border-left: 4px solid #409eff;
    background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .my-tag {
      font-weight: 600;
    }
  }
}

.reward {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.task-title {
  margin: 0 0 12px;
  font-size: 16px;
  color: #333;
}

.task-address {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  color: #999;
  font-size: 12px;
}
</style>
