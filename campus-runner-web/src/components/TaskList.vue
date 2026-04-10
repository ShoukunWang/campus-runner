<template>
  <div class="task-list" v-loading="loading">
    <el-empty v-if="!tasks || tasks.length === 0" description="暂无任务" />
    
    <el-card v-for="task in tasks" :key="task.id" class="task-card" @click="goDetail(task.id)">
      <div class="task-header">
        <h4>{{ task.title }}</h4>
        <span class="reward">¥{{ task.reward }}</span>
      </div>
      <p class="task-address">{{ task.pickupAddress }} → {{ task.deliveryAddress }}</p>
      <div class="task-footer">
        <el-tag :type="getStatusType(task.status)" size="small">{{ getStatusText(task.status) }}</el-tag>
        <span class="time">{{ formatTime(task.createTime) }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps<{
  tasks: any[]
  loading: boolean
}>()

const router = useRouter()

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待接单', 1: '进行中', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleDateString()
}

const goDetail = (id: string) => {
  router.push(`/task/${id}`)
}
</script>

<style scoped lang="scss">
.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  h4 {
    margin: 0;
    font-size: 16px;
  }
}

.reward {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.task-address {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
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
