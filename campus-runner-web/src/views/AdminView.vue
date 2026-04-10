<template>
  <div class="admin-container">
    <!-- 管理员子菜单 -->
    <div class="admin-submenu">
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="admin-menu-horizontal"
      >
        <el-menu-item index="/admin">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/admins">
          <el-icon><Setting /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="admin-content">
          <!-- 数据概览 -->
          <div v-if="$route.path === '/admin'" class="dashboard">
            <!-- 统计卡片 -->
            <el-row :gutter="20" class="stat-row">
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-users">
                    <el-icon :size="32"><User /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">{{ stats.overview?.totalUsers || 0 }}</div>
                    <div class="stat-label">总用户数</div>
                    <div class="stat-trend">
                      <span class="trend-up">+{{ stats.today?.newUsers || 0 }}</span>
                      <span class="trend-label">今日新增</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-orders">
                    <el-icon :size="32"><Document /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">{{ stats.overview?.totalTasks || 0 }}</div>
                    <div class="stat-label">总订单数</div>
                    <div class="stat-trend">
                      <span class="trend-up">+{{ stats.today?.newTasks || 0 }}</span>
                      <span class="trend-label">今日新增</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-revenue">
                    <el-icon :size="32"><Money /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">¥{{ stats.overview?.totalRevenue || 0 }}</div>
                    <div class="stat-label">总收益</div>
                    <div class="stat-trend">
                      <span class="trend-up">+¥{{ stats.today?.revenue || 0 }}</span>
                      <span class="trend-label">今日收益</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="stat-row">
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-ongoing">
                    <el-icon :size="32"><Clock /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">{{ stats.overview?.totalOngoingTasks || 0 }}</div>
                    <div class="stat-label">进行中订单</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-completed">
                    <el-icon :size="32"><Select /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">{{ stats.overview?.totalCompletedTasks || 0 }}</div>
                    <div class="stat-label">已完成订单</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card-v2" shadow="hover">
                  <div class="stat-icon icon-rate">
                    <el-icon :size="32"><TrendCharts /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-value">{{ stats.overview?.completionRate || 0 }}%</div>
                    <div class="stat-label">任务完成率</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 图表区域 -->
            <el-row :gutter="20" class="chart-row">
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>任务状态分布</span>
                    </div>
                  </template>
                  <div class="status-distribution">
                    <div class="status-item" v-for="item in statusDistribution" :key="item.name">
                      <div class="status-info">
                        <span class="status-name">{{ item.name }}</span>
                        <span class="status-count">{{ item.count }} ({{ item.percentage }}%)</span>
                      </div>
                      <div class="status-bar">
                        <div class="status-fill" :style="{ width: item.percentage + '%', backgroundColor: item.color }"></div>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>近7天任务趋势</span>
                    </div>
                  </template>
                  <div class="trend-chart">
                    <div class="trend-bar" v-for="day in last7DaysTrend" :key="day.date">
                      <div class="bar-fill" :style="{ height: day.height + '%' }"></div>
                      <div class="bar-label">{{ day.label }}</div>
                      <div class="bar-value">{{ day.count }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 用户管理 -->
          <div v-else-if="$route.path === '/admin/users'" class="users-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>用户列表</span>
                  <el-input
                    v-model="userSearch"
                    placeholder="搜索用户"
                    style="width: 200px"
                    size="small"
                    @keyup.enter="loadUserList"
                  />
                </div>
              </template>
              <el-table :data="userList" style="width: 100%">
                <el-table-column prop="username" label="用户名" />
                <el-table-column prop="nickname" label="昵称" />
                <el-table-column prop="phone" label="手机号" />
                <el-table-column prop="creditScore" label="信用分" width="100" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                      {{ scope.row.status === 1 ? '正常' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="openEditUserDialog(scope.row)">
                      编辑
                    </el-button>
                    <el-button 
                      :type="scope.row.status === 1 ? 'danger' : 'success'" 
                      size="small"
                      @click="toggleUserStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '禁用' : '启用' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <!-- 任务管理 -->
          <div v-else-if="$route.path === '/admin/tasks'" class="tasks-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>任务列表</span>
                  <div class="task-filter-bar">
                    <el-input
                      v-model="taskSearch.keyword"
                      placeholder="搜索任务编号/标题"
                      clearable
                      style="width: 250px"
                      @clear="loadTaskList"
                      @keyup.enter="loadTaskList"
                    >
                      <template #prefix><el-icon><Search /></el-icon></template>
                    </el-input>
                    
                    <el-select v-model="taskSearch.status" placeholder="任务状态" clearable style="width: 150px" @change="loadTaskList">
                      <el-option label="待接取" :value="0" />
                      <el-option label="已接取/已取件" :value="1" />
                      <el-option label="已完成" :value="2" />
                      <el-option label="已取消" :value="3" />
                    </el-select>
                    
                    <el-button type="primary" @click="loadTaskList">
                      <el-icon><Search /></el-icon>
                      搜索
                    </el-button>
                    
                    <el-button @click="resetTaskSearch">重置</el-button>
                  </div>
                </div>
              </template>
              <el-table :data="taskList" style="width: 100%">
                <el-table-column prop="taskNo" label="任务编号" width="150" />
                <el-table-column prop="title" label="任务标题" min-width="200" />
                <el-table-column prop="reward" label="酬金" width="100">
                  <template #default="scope">
                    ¥{{ scope.row.reward }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="getTaskStatusType(scope.row.status)">
                      {{ getTaskProgressText(scope.row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="pickupTime" label="取件时间" width="160">
                  <template #default="scope">
                    {{ scope.row.pickupTime ? formatDateTime(scope.row.pickupTime) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="deliverTime" label="送达时间" width="160">
                  <template #default="scope">
                    {{ scope.row.deliverTime ? formatDateTime(scope.row.deliverTime) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="openEditTaskDialog(scope.row)">
                      编辑
                    </el-button>
                    <el-button type="danger" size="small" @click="handleDeleteTask(scope.row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <!-- 管理员管理 -->
          <div v-else-if="$route.path === '/admin/admins'" class="admins-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>管理员列表</span>
                  <div>
                    <el-input
                      v-model="adminSearch"
                      placeholder="搜索管理员"
                      style="width: 200px; margin-right: 10px"
                      size="small"
                      @keyup.enter="loadAdminList"
                    />
                    <el-button type="primary" size="small" @click="openAddAdminDialog">
                      添加管理员
                    </el-button>
                  </div>
                </div>
              </template>
              <el-table :data="adminList" style="width: 100%">
                <el-table-column prop="username" label="用户名" />
                <el-table-column prop="realName" label="姓名" />
                <el-table-column prop="phone" label="手机号" />
                <el-table-column prop="role" label="角色" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.role === 1 ? 'danger' : 'info'">
                      {{ scope.row.role === 1 ? '超级管理员' : '普通管理员' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                      {{ scope.row.status === 1 ? '正常' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="openEditAdminDialog(scope.row)">
                      编辑
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
    </div>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editUserDialogVisible" title="编辑用户信息" width="500px">
      <el-form :model="editUserForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editUserForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editUserForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editUserForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUserForm.email" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="editUserForm.creditScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editUserDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑任务对话框 -->
    <el-dialog v-model="editTaskDialogVisible" title="编辑任务" width="600px">
      <el-form :model="editTaskForm" label-width="80px">
        <el-form-item label="任务编号">
          <el-input v-model="editTaskForm.taskNo" disabled />
        </el-form-item>
        <el-form-item label="任务标题">
          <el-input v-model="editTaskForm.title" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="editTaskForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="酬金">
          <el-input-number v-model="editTaskForm.reward" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editTaskForm.status">
            <el-option label="待接单" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editTaskForm.status === 3" label="取消原因">
          <el-input v-model="editTaskForm.cancelReason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editTaskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog v-model="adminDialogVisible" :title="isAdminAdd ? '添加管理员' : '编辑管理员'" width="500px">
      <el-form :model="adminForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="adminForm.username" :disabled="!isAdminAdd" />
        </el-form-item>
        <el-form-item v-if="isAdminAdd" label="密码">
          <el-input v-model="adminForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="adminForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="adminForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="adminForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="adminForm.role">
            <el-option label="普通管理员" :value="0" />
            <el-option label="超级管理员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isAdminAdd" label="状态">
          <el-select v-model="adminForm.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAdmin">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataLine, User, List, Setting, Document, Money, Clock, Select, TrendCharts, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { 
  getStatistics, 
  getUserList, 
  updateUserStatus, 
  updateUserInfo,
  getAdminTaskList, 
  deleteAdminTask as deleteTaskApi,
  updateAdminTask,
  getAdminList,
  addAdmin,
  updateAdmin
} from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 统计数据
const stats = ref<any>({
  overview: {},
  today: {},
  taskStatusStats: {},
  last7DaysTrend: []
})

// 任务状态分布
const statusDistribution = computed(() => {
  const statusMap = [
    { key: 'pending', name: '待接取', color: '#909399' },
    { key: 'ongoing', name: '进行中', color: '#e6a23c' },
    { key: 'completed', name: '已完成', color: '#67c23a' },
    { key: 'cancelled', name: '已取消', color: '#f56c6c' }
  ]
  
  const total = Object.values(stats.value.taskStatusStats).reduce((sum: number, val: any) => sum + val, 0) || 1
  
  return statusMap.map(item => ({
    name: item.name,
    color: item.color,
    count: stats.value.taskStatusStats[item.key] || 0,
    percentage: total > 0 ? Math.round((stats.value.taskStatusStats[item.key] || 0) / total * 100) : 0
  }))
})

// 近7天趋势
const last7DaysTrend = computed(() => {
  const trend = stats.value.last7DaysTrend || []
  const maxCount = Math.max(...trend.map((d: any) => d.count), 1)
  
  return trend.map((day: any) => ({
    ...day,
    height: (day.count / maxCount) * 100
  }))
})

// 用户管理
const userSearch = ref('')
const userList = ref([])
const userPageNum = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)

// 任务管理
const taskSearch = ref({
  keyword: '',
  status: undefined as number | undefined
})
const taskList = ref([])
const taskPageNum = ref(1)
const taskPageSize = ref(10)
const taskTotal = ref(0)

// 重置任务搜索
const resetTaskSearch = () => {
  taskSearch.value = {
    keyword: '',
    status: undefined
  }
  loadTaskList()
}

// 获取任务进度文本
const getTaskProgressText = (task: any) => {
  if (task.status === 0) return '待接取'
  if (task.status === 1 && !task.pickupTime) return '已接取'
  if (task.status === 1 && task.pickupTime) return '已取件'
  if (task.status === 2) return '已完成'
  if (task.status === 3) return '已取消'
  return '未知'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 管理员管理
const adminSearch = ref('')
const adminList = ref([])
const adminPageNum = ref(1)
const adminPageSize = ref(10)
const adminTotal = ref(0)

// 编辑用户对话框
const editUserDialogVisible = ref(false)
const editUserForm = ref({
  id: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  creditScore: 100
})

// 编辑任务对话框
const editTaskDialogVisible = ref(false)
const editTaskForm = ref({
  id: '',
  taskNo: '',
  title: '',
  description: '',
  reward: 0,
  status: 0,
  cancelReason: ''
})

// 管理员对话框
const adminDialogVisible = ref(false)
const isAdminAdd = ref(true)
const adminForm = ref({
  id: '',
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 0,
  status: 1
})

const getTaskStatusType = (status: number) => {
  const types = ['info', 'warning', 'success', 'danger']
  return types[status] || 'info'
}

const getTaskStatusText = (status: number) => {
  const texts = ['待接单', '进行中', '已完成', '已取消']
  return texts[status] || '未知'
}

const loadStatistics = async () => {
  try {
    console.log('开始加载统计数据...')
    const res: any = await getStatistics()
    console.log('统计数据响应:', res)
    if (res.result) {
      // 后端返回嵌套结构，直接使用
      stats.value = {
        overview: res.result.overview || {},
        today: res.result.today || {},
        taskStatusStats: res.result.taskStatusStats || {},
        last7DaysTrend: res.result.last7DaysTrend || []
      }
      console.log('统计数据已更新:', stats.value)
    } else {
      console.warn('统计数据为空')
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const loadUserList = async () => {
  try {
    const res: any = await getUserList({
      pageNum: userPageNum.value,
      pageSize: userPageSize.value,
      keyword: userSearch.value || undefined
    })
    if (res.result) {
      userList.value = res.result.records || []
      userTotal.value = res.result.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('获取用户列表失败')
  }
}

const toggleUserStatus = async (user: any) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`已${newStatus === 1 ? '启用' : '禁用'}用户`)
    user.status = newStatus
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 打开编辑用户对话框
const openEditUserDialog = (user: any) => {
  editUserForm.value = {
    id: user.id,
    username: user.username,
    nickname: user.nickname || '',
    phone: user.phone || '',
    email: user.email || '',
    creditScore: user.creditScore || 100
  }
  editUserDialogVisible.value = true
}

// 保存用户信息修改
const handleEditUser = async () => {
  try {
    await updateUserInfo(editUserForm.value.id, {
      nickname: editUserForm.value.nickname,
      phone: editUserForm.value.phone,
      email: editUserForm.value.email,
      creditScore: editUserForm.value.creditScore
    })
    ElMessage.success('修改成功')
    editUserDialogVisible.value = false
    loadUserList()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const loadTaskList = async () => {
  try {
    const res: any = await getAdminTaskList({
      pageNum: taskPageNum.value,
      pageSize: taskPageSize.value,
      taskNo: taskSearch.value.keyword || undefined,
      title: taskSearch.value.keyword || undefined,
      status: taskSearch.value.status
    })
    if (res.result) {
      taskList.value = res.result.records || []
      taskTotal.value = res.result.total || 0
    }
  } catch (error) {
    console.error('获取任务列表失败', error)
    ElMessage.error('获取任务列表失败')
  }
}

const handleDeleteTask = async (task: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTaskApi(task.id)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 打开编辑任务对话框
const openEditTaskDialog = (task: any) => {
  editTaskForm.value = {
    id: task.id,
    taskNo: task.taskNo,
    title: task.title || '',
    description: task.description || '',
    reward: task.reward || 0,
    status: task.status,
    cancelReason: task.cancelReason || ''
  }
  editTaskDialogVisible.value = true
}

// 保存任务修改
const handleEditTask = async () => {
  try {
    await updateAdminTask(editTaskForm.value.id, {
      title: editTaskForm.value.title,
      description: editTaskForm.value.description,
      reward: editTaskForm.value.reward,
      status: editTaskForm.value.status,
      cancelReason: editTaskForm.value.cancelReason
    })
    ElMessage.success('修改成功')
    editTaskDialogVisible.value = false
    loadTaskList()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 加载管理员列表
const loadAdminList = async () => {
  try {
    const res: any = await getAdminList({
      pageNum: adminPageNum.value,
      pageSize: adminPageSize.value,
      keyword: adminSearch.value || undefined
    })
    if (res.result) {
      adminList.value = res.result.records || []
      adminTotal.value = res.result.total || 0
    }
  } catch (error) {
    console.error('获取管理员列表失败', error)
    ElMessage.error('获取管理员列表失败')
  }
}

// 打开添加管理员对话框
const openAddAdminDialog = () => {
  isAdminAdd.value = true
  adminForm.value = {
    id: '',
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    role: 0,
    status: 1
  }
  adminDialogVisible.value = true
}

// 打开编辑管理员对话框
const openEditAdminDialog = (admin: any) => {
  isAdminAdd.value = false
  adminForm.value = {
    id: admin.id,
    username: admin.username,
    password: '',
    realName: admin.realName || '',
    phone: admin.phone || '',
    email: admin.email || '',
    role: admin.role,
    status: admin.status
  }
  adminDialogVisible.value = true
}

// 保存管理员
const handleSaveAdmin = async () => {
  try {
    if (isAdminAdd.value) {
      if (!adminForm.value.username || !adminForm.value.password) {
        ElMessage.warning('用户名和密码不能为空')
        return
      }
      await addAdmin({
        username: adminForm.value.username,
        password: adminForm.value.password,
        realName: adminForm.value.realName,
        phone: adminForm.value.phone,
        email: adminForm.value.email,
        role: adminForm.value.role
      })
      ElMessage.success('添加成功')
    } else {
      await updateAdmin(adminForm.value.id, {
        realName: adminForm.value.realName,
        phone: adminForm.value.phone,
        email: adminForm.value.email,
        role: adminForm.value.role,
        status: adminForm.value.status
      })
      ElMessage.success('修改成功')
    }
    adminDialogVisible.value = false
    loadAdminList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadStatistics()
})

// 监听路由变化加载对应数据
import { watch } from 'vue'
watch(() => route.path, (newPath) => {
  if (newPath === '/admin') {
    loadStatistics()
  } else if (newPath === '/admin/users') {
    loadUserList()
  } else if (newPath === '/admin/tasks') {
    loadTaskList()
  } else if (newPath === '/admin/admins') {
    loadAdminList()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.admin-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  
  // 管理员子菜单
  .admin-submenu {
    margin-bottom: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .admin-menu-horizontal {
      border-bottom: none;

      :deep(.el-menu-item) {
        height: 50px;
        line-height: 50px;

        &.is-active {
          color: #e6a23c;
          border-bottom: 2px solid #e6a23c;
          background-color: rgba(230, 162, 60, 0.05);
        }

        &:hover {
          color: #e6a23c;
        }
      }
    }
  }

  .admin-content {
    background: transparent;
  }
}

// 现代化统计卡片
.stat-row {
  margin-bottom: 20px;
}

.stat-card-v2 {
  border-radius: 12px;
  border: none;
  transition: all 0.3s;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0,0,0,0.15);
  }
  
  :deep(.el-card__body) {
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
    
    &.icon-users {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &.icon-orders {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    &.icon-revenue {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    
    &.icon-ongoing {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }
    
    &.icon-completed {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }
    
    &.icon-rate {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      color: #333;
    }
  }
  
  .stat-content {
    flex: 1;
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .stat-label {
      color: #909399;
      font-size: 14px;
      margin-bottom: 8px;
    }
    
    .stat-trend {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      
      .trend-up {
        color: #67c23a;
        font-weight: 600;
      }
      
      .trend-label {
        color: #909399;
      }
    }
  }
}

// 图表卡片
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  
  .card-header {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
  }
  
  // 状态分布
  .status-distribution {
    padding: 10px 0;
    
    .status-item {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .status-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;
        
        .status-name {
          color: #606266;
          font-weight: 500;
        }
        
        .status-count {
          color: #909399;
        }
      }
      
      .status-bar {
        height: 8px;
        background: #f0f0f0;
        border-radius: 4px;
        overflow: hidden;
        
        .status-fill {
          height: 100%;
          border-radius: 4px;
          transition: width 0.5s ease;
        }
      }
    }
  }
  
  // 趋势图
  .trend-chart {
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: 200px;
    padding: 20px 10px 40px;
    position: relative;
    
    .trend-bar {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;
      height: 100%;
      justify-content: flex-end;
      position: relative;
      
      .bar-fill {
        width: 40px;
        background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
        border-radius: 4px 4px 0 0;
        transition: height 0.5s ease;
        min-height: 4px;
      }
      
      .bar-label {
        position: absolute;
        bottom: -30px;
        font-size: 12px;
        color: #909399;
      }
      
      .bar-value {
        position: absolute;
        top: -25px;
        font-size: 12px;
        color: #606266;
        font-weight: 600;
      }
    }
  }
}

// 任务搜索栏
.task-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

// 表格样式优化
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  
  :deep(.el-table__header) {
    th {
      background: #fafafa;
      font-weight: 600;
      color: #303133;
    }
  }
  
  :deep(.el-table__row) {
    &:hover {
      background: #f5f7fa;
    }
  }
}

.chart-placeholder {
  padding: 40px;
  text-align: center;
}

.progress-text {
  margin-top: 16px;
  color: #666;
}

.notice-list {
  p {
    margin: 8px 0;
    color: #666;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
