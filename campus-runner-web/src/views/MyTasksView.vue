<template>
  <div class="my-tasks-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 我发布的任务 -->
      <el-tab-pane label="我发布的" name="published">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else>
          <!-- 状态筛选标签 -->
          <div class="status-filter">
            <el-radio-group v-model="publishedActiveStatus" size="large">
              <el-radio-button
                v-for="item in publishedStatusOptions"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }} ({{ item.count }})
              </el-radio-button>
            </el-radio-group>
          </div>

          <!-- 任务列表 -->
          <task-list
            :tasks="filteredPublishedTasks"
            :loading="false"
          />

          <el-empty v-if="filteredPublishedTasks.length === 0" description="暂无该状态的任务" />
        </div>
      </el-tab-pane>

      <!-- 我接受的任务 -->
      <el-tab-pane label="我接受的" name="accepted">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else>
          <!-- 状态筛选标签 -->
          <div class="status-filter">
            <el-radio-group v-model="acceptedActiveStatus" size="large">
              <el-radio-button
                v-for="item in acceptedStatusOptions"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }} ({{ item.count }})
              </el-radio-button>
            </el-radio-group>
          </div>

          <!-- 任务列表 -->
          <task-list
            :tasks="filteredAcceptedTasks"
            :loading="false"
          />

          <el-empty v-if="filteredAcceptedTasks.length === 0" description="暂无该状态的任务" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'

import { getMyTasks } from '@/api/task'
import TaskList from '@/components/TaskList.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const MY_TASKS_TAB_KEY = 'my-tasks-active-tab'

// 根据身份确定默认标签页
const getDefaultTab = () => {
  return userStore.currentRole === 'publisher' ? 'published' : 'accepted'
}

// 从 sessionStorage 恢复标签页状态
const savedTab = sessionStorage.getItem(MY_TASKS_TAB_KEY)
const activeTab = ref(savedTab || getDefaultTab())
const loading = ref(false)
const publishedTasks = ref<any[]>([])
const acceptedTasks = ref<any[]>([])

// 当前选中的状态筛选
const publishedActiveStatus = ref<number | 'all'>('all')
const acceptedActiveStatus = ref<number | 'all'>('all')

// 状态标签配置（我发布的）
const publishedStatusConfig = [
  { value: 'all', label: '全部', status: null },
  { value: 0, label: '待接单', type: 'info' },
  { value: 1, label: '进行中', type: 'warning' },
  { value: 2, label: '已完成', type: 'success' },
  { value: 3, label: '已取消', type: 'danger' }
]

// 状态标签配置（我接受的）
const acceptedStatusConfig = [
  { value: 'all', label: '全部', status: null },
  { value: 1, label: '进行中', type: 'warning' },
  { value: 2, label: '已完成', type: 'success' },
  { value: 3, label: '已取消', type: 'danger' }
]

// 按状态分组的计算属性
const publishedTasksByStatus = computed(() => {
  const groups: Record<number, any[]> = { 0: [], 1: [], 2: [], 3: [] }
  publishedTasks.value.forEach(task => {
    const status = task.status || 0
    if (!groups[status]) groups[status] = []
    groups[status].push(task)
  })
  return groups
})

const acceptedTasksByStatus = computed(() => {
  const groups: Record<number, any[]> = { 0: [], 1: [], 2: [], 3: [] }
  acceptedTasks.value.forEach(task => {
    const status = task.status || 0
    if (!groups[status]) groups[status] = []
    groups[status].push(task)
  })
  return groups
})

// 状态选项（带数量）
const publishedStatusOptions = computed(() => {
  return publishedStatusConfig.map(item => ({
    ...item,
    count: item.value === 'all'
      ? publishedTasks.value.length
      : (publishedTasksByStatus.value[item.value as number] || []).length
  }))
})

const acceptedStatusOptions = computed(() => {
  return acceptedStatusConfig.map(item => ({
    ...item,
    count: item.value === 'all'
      ? acceptedTasks.value.length
      : (acceptedTasksByStatus.value[item.value as number] || []).length
  }))
})

// 筛选后的任务列表
const filteredPublishedTasks = computed(() => {
  if (publishedActiveStatus.value === 'all') {
    return publishedTasks.value
  }
  return publishedTasksByStatus.value[publishedActiveStatus.value as number] || []
})

const filteredAcceptedTasks = computed(() => {
  if (acceptedActiveStatus.value === 'all') {
    return acceptedTasks.value
  }
  return acceptedTasksByStatus.value[acceptedActiveStatus.value as number] || []
})

onMounted(() => {
  loadTasks()
})

const loadTasks = async () => {
  loading.value = true
  try {
    // 加载发布的任务
    const publishedRes: any = await getMyTasks({ type: 'published' })
    publishedTasks.value = publishedRes?.result?.records || publishedRes?.result?.list || []

    // 加载接受的任务
    const acceptedRes: any = await getMyTasks({ type: 'accepted' })
    acceptedTasks.value = acceptedRes?.result?.records || acceptedRes?.result?.list || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tabName: string) => {
  // 保存当前选中的标签页到 sessionStorage
  sessionStorage.setItem(MY_TASKS_TAB_KEY, tabName)
  loadTasks()
}

// 当身份切换时,如果没有手动选择过标签页,自动切换
watch(() => userStore.currentRole, (newRole) => {
  const hasManualSelection = sessionStorage.getItem(MY_TASKS_TAB_KEY)
  if (!hasManualSelection) {
    activeTab.value = newRole === 'publisher' ? 'published' : 'accepted'
  } else {
    // 如果有手动选择,清除sessionStorage,让身份切换生效
    sessionStorage.removeItem(MY_TASKS_TAB_KEY)
    activeTab.value = newRole === 'publisher' ? 'published' : 'accepted'
  }
})
</script>

<style scoped lang="scss">
.my-tasks-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  padding: 20px;
}

.status-filter {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;

  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  :deep(.el-radio-button__inner) {
    padding: 8px 16px;
  }
}
</style>
