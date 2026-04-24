<template>
  <div class="favorites-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else>
      <div v-if="favorites.length > 0" class="favorites-list">
        <el-card
          v-for="item in favorites"
          :key="item.favoriteId"
          class="favorite-card"
          shadow="hover"
          @click="goToTaskDetail(item.taskId)"
        >
          <div class="card-content">
            <div class="task-info">
              <h4>{{ item.title }}</h4>
              <p class="task-no">任务编号: {{ item.taskNo }}</p>
              <div class="address">
                <p><el-icon><Location /></el-icon> 取件: {{ item.pickupAddress }}</p>
                <p><el-icon><Location /></el-icon> 送达: {{ item.deliveryAddress }}</p>
              </div>
            </div>
            <div class="task-meta">
              <span class="reward">¥{{ item.reward }}</span>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
              <el-button
                type="danger"
                size="small"
                plain
                @click.stop="handleCancelFavorite(item.taskId)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
          <div class="favorite-time">
            收藏于: {{ item.createTime }}
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无收藏的任务" />

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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getFavoriteList, toggleFavorite } from '@/api/favorite'

const router = useRouter()
const loading = ref(false)
const favorites = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadFavorites()
})

const loadFavorites = async () => {
  loading.value = true
  try {
    const res: any = await getFavoriteList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    favorites.value = res.result?.list || []
    total.value = res.result?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
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

const goToTaskDetail = (taskId: string) => {
  router.push(`/task/${taskId}`)
}

const handleCancelFavorite = async (taskId: string) => {
  try {
    await ElMessageBox.confirm('确定取消收藏该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleFavorite(taskId)
    ElMessage.success('取消收藏成功')
    loadFavorites()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  pageNum.value = 1
  loadFavorites()
}

const handlePageChange = (val: number) => {
  pageNum.value = val
  loadFavorites()
}
</script>

<style scoped lang="scss">
.favorites-container {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  padding: 20px;
}

.favorites-list {
  display: grid;
  gap: 16px;
}

.favorite-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .card-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;

    .task-info {
      flex: 1;

      h4 {
        margin: 0 0 8px;
        font-size: 16px;
        color: #303133;
      }

      .task-no {
        margin: 0 0 8px;
        font-size: 12px;
        color: #909399;
      }

      .address {
        p {
          margin: 4px 0;
          font-size: 13px;
          color: #606266;
          display: flex;
          align-items: center;
          gap: 4px;

          .el-icon {
            color: #409eff;
          }
        }
      }
    }

    .task-meta {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 8px;

      .reward {
        color: #f56c6c;
        font-size: 20px;
        font-weight: 600;
      }
    }
  }

  .favorite-time {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    font-size: 12px;
    color: #909399;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
