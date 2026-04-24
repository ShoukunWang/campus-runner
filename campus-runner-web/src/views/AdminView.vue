<template>
  <div class="admin-container">
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
        <el-row :gutter="20" class="stat-cards">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="stat-card total-users">
              <div class="stat-icon">
                <el-icon :size="32"><UserFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">↑ 12%</span>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="stat-card total-tasks">
              <div class="stat-icon">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalTasks || 0 }}</div>
                <div class="stat-label">总订单数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">↑ 8%</span>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="stat-card completed-tasks">
              <div class="stat-icon">
                <el-icon :size="32"><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completedTasks || 0 }}</div>
                <div class="stat-label">已完成订单</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">↑ 15%</span>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="stat-card total-amount">
              <div class="stat-icon">
                <el-icon :size="32"><Money /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">¥{{ stats.totalReward || 0 }}</div>
                <div class="stat-label">交易总额</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">↑ 20%</span>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-20">
          <el-col :xs="24" :md="12">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header-title">
                  <span>任务状态分布</span>
                  <el-button type="primary" link size="small" @click="loadStats">刷新</el-button>
                </div>
              </template>
              <div class="status-distribution">
                <div class="progress-item">
                  <div class="progress-label">
                    <span>待接单</span>
                    <span class="num">{{ stats.pendingTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.pendingTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#909399"
                  />
                </div>
                <div class="progress-item">
                  <div class="progress-label">
                    <span>进行中</span>
                    <span class="num">{{ stats.runningTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.runningTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#e6a23c"
                  />
                </div>
                <div class="progress-item">
                  <div class="progress-label">
                    <span>已完成</span>
                    <span class="num">{{ stats.completedTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.completedTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#67c23a"
                  />
                </div>
                <div class="progress-item">
                  <div class="progress-label">
                    <span>已取消</span>
                    <span class="num">{{ stats.cancelledTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.cancelledTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#f56c6c"
                  />
                </div>
              </div>
              <div class="completion-rate">
                <div class="rate-circle">
                  <el-progress 
                    type="circle"
                    :percentage="taskCompletionRate"
                    :stroke-width="10"
                    color="#67c23a"
                  />
                  <p>任务完成率</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header-title">
                  <span>分类任务统计</span>
                </div>
              </template>
              <div class="category-stats">
                <div 
                  v-for="(item, index) in categoryStats" 
                  :key="index"
                  class="category-item"
                >
                  <div class="category-name">{{ item.name }}</div>
                  <div class="category-bar">
                    <div 
                      class="bar-fill" 
                      :style="{ 
                        width: (item.count / maxCategoryCount * 100) + '%',
                        backgroundColor: categoryColors[index % categoryColors.length]
                      }"
                    ></div>
                    <span class="bar-count">{{ item.count }} 单</span>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card shadow="hover" class="mt-20">
              <template #header>
                <span>重量等级分布</span>
              </template>
              <div class="weight-stats">
                <el-row :gutter="10">
                  <el-col :span="6" v-for="(item, index) in weightStats" :key="index">
                    <div class="weight-item" :style="{ borderColor: weightColors[index] }">
                      <div class="weight-label">{{ item.label }}</div>
                      <div class="weight-count">{{ item.count }}</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-20">
          <el-col :span="24">
            <el-card shadow="hover">
              <template #header>
                <span>最近任务</span>
              </template>
              <el-table :data="recentTasks" size="small" style="width: 100%">
                <el-table-column prop="taskNo" label="任务编号" width="150" />
                <el-table-column prop="title" label="任务标题" show-overflow-tooltip />
                <el-table-column prop="reward" label="酬金" width="100" align="center">
                  <template #default="scope">¥{{ scope.row.reward }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag size="small" :type="getTaskStatusType(scope.row.status)">
                      {{ getTaskStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 用户管理 -->
      <div v-else-if="$route.path === '/admin/users'" class="users-management">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户列表</span>
              <div class="header-actions">
                <el-input
                  v-model="userSearch"
                  placeholder="搜索用户名/昵称/手机号"
                  style="width: 220px"
                  size="small"
                  clearable
                  @keyup.enter="loadUserList"
                  :prefix-icon="Search"
                />
                <el-select v-model="userStatusFilter" placeholder="用户状态" size="small" clearable style="width: 100px">
                  <el-option label="正常" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
                <el-button type="primary" size="small" @click="loadUserList" :icon="Refresh">查询</el-button>
              </div>
            </div>
          </template>
          <el-table :data="userList" style="width: 100%" v-loading="userLoading">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" width="120" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
            <el-table-column prop="creditScore" label="信用分" width="100" align="center">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.creditScore >= 80 ? 'success' : scope.row.creditScore >= 60 ? 'warning' : 'danger'">
                  {{ scope.row.creditScore }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
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
          <el-pagination
            v-model:current-page="userPageNum"
            v-model:page-size="userPageSize"
            :total="userTotal"
            layout="total, prev, pager, next, jumper"
            @change="loadUserList"
            class="table-pagination"
          />
        </el-card>
      </div>

      <!-- 任务管理 -->
      <div v-else-if="$route.path === '/admin/tasks'" class="tasks-management">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>任务列表</span>
              <div class="header-actions">
                <el-input
                  v-model="taskSearch.taskNo"
                  placeholder="任务编号"
                  style="width: 150px"
                  size="small"
                  clearable
                  :prefix-icon="Document"
                />
                <el-input
                  v-model="taskSearch.title"
                  placeholder="任务标题关键词"
                  style="width: 180px"
                  size="small"
                  clearable
                  @keyup.enter="loadTaskList"
                  :prefix-icon="Search"
                />
                <el-select v-model="taskSearch.status" placeholder="任务状态" size="small" style="width: 120px" clearable>
                  <el-option label="全部" :value="" />
                  <el-option label="待接单" :value="0" />
                  <el-option label="进行中" :value="1" />
                  <el-option label="已完成" :value="2" />
                  <el-option label="已取消" :value="3" />
                </el-select>
                <el-button type="primary" size="small" @click="loadTaskList" :icon="Search">查询</el-button>
                <el-button size="small" @click="resetTaskSearch" :icon="Refresh">重置</el-button>
              </div>
            </div>
          </template>
          <el-table :data="taskList" style="width: 100%" v-loading="taskLoading">
            <el-table-column prop="taskNo" label="任务编号" width="150" />
            <el-table-column prop="title" label="任务标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="reward" label="酬金" width="100" align="center">
              <template #default="scope">
                <span class="reward-text">¥{{ scope.row.reward }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="weightLevel" label="重量等级" width="100" align="center">
              <template #default="scope">
                {{ getWeightLevelText(scope.row.weightLevel) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag size="small" :type="getTaskStatusType(scope.row.status)">
                  {{ getTaskStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
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
          <el-pagination
            v-model:current-page="taskPageNum"
            v-model:page-size="taskPageSize"
            :total="taskTotal"
            layout="total, prev, pager, next, jumper"
            @change="loadTaskList"
            class="table-pagination"
          />
        </el-card>
      </div>

      <!-- 管理员管理 -->
      <div v-else-if="$route.path === '/admin/admins'" class="admins-management">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>管理员列表</span>
              <div class="header-actions">
                <el-input
                  v-model="adminSearch"
                  placeholder="搜索管理员"
                  style="width: 200px; margin-right: 10px"
                  size="small"
                  clearable
                  @keyup.enter="loadAdminList"
                  :prefix-icon="Search"
                />
                <el-button type="primary" size="small" @click="openAddAdminDialog">
                  添加管理员
                </el-button>
              </div>
            </div>
          </template>
          <el-table :data="adminList" style="width: 100%" v-loading="adminLoading">
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
            <el-table-column prop="role" label="角色" width="140" align="center">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.role === 1 ? 'danger' : 'info'">
                  {{ scope.row.role === 1 ? '超级管理员' : '普通管理员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openEditAdminDialog(scope.row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="adminPageNum"
            v-model:page-size="adminPageSize"
            :total="adminTotal"
            layout="total, prev, pager, next, jumper"
            @change="loadAdminList"
            class="table-pagination"
          />
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
            <el-option label="普通管理员" :value="2" />
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
import { 
  DataLine, User, List, Setting, Search, Refresh, Document, 
  UserFilled, CircleCheck, Money
} from '@element-plus/icons-vue'
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

// 统计数据
const stats = ref<any>({})
const recentTasks = ref<any[]>([])
const categoryStats = ref([
  { name: '代取快递', count: 0 },
  { name: '代购', count: 0 },
  { name: '跨校区送件', count: 0 },
  { name: '文件传递', count: 0 },
  { name: '其他', count: 0 }
])
const weightStats = ref([
  { label: '小件', count: 0 },
  { label: '中件', count: 0 },
  { label: '大件', count: 0 },
  { label: '超大件', count: 0 }
])

const categoryColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
const weightColors = ['#67c23a', '#e6a23c', '#f56c6c', '#909399']

const maxCategoryCount = computed(() => {
  return Math.max(...categoryStats.value.map(c => c.count), 1)
})

const taskCompletionRate = computed(() => {
  const total = stats.value.totalTasks || 0
  const completed = stats.value.completedTasks || 0
  return total > 0 ? Math.round(completed / total * 100) : 0
})

// 用户管理
const userLoading = ref(false)
const userList = ref<any[]>([])
const userSearch = ref('')
const userStatusFilter = ref<number | ''>('')
const userTotal = ref(0)
const userPageNum = ref(1)
const userPageSize = ref(10)
const editUserDialogVisible = ref(false)
const editUserForm = ref<any>({})

// 任务管理
const taskLoading = ref(false)
const taskList = ref<any[]>([])
const taskSearch = ref({
  taskNo: '',
  title: '',
  status: '' as number | ''
})
const taskTotal = ref(0)
const taskPageNum = ref(1)
const taskPageSize = ref(10)
const editTaskDialogVisible = ref(false)
const editTaskForm = ref<any>({})

// 管理员管理
const adminLoading = ref(false)
const adminList = ref<any[]>([])
const adminSearch = ref('')
const adminTotal = ref(0)
const adminPageNum = ref(1)
const adminPageSize = ref(10)
const adminDialogVisible = ref(false)
const isAdminAdd = ref(false)
const adminForm = ref<any>({})

const activeMenu = computed(() => route.path)

onMounted(() => {
  if (route.path === '/admin') {
    loadStats()
  } else if (route.path === '/admin/users') {
    loadUserList()
  } else if (route.path === '/admin/tasks') {
    loadTaskList()
  } else if (route.path === '/admin/admins') {
    loadAdminList()
  }
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res: any = await getStatistics()
    stats.value = res.result || {}
    
    recentTasks.value = stats.value.recentTasks || []
    
    if (stats.value.categoryStats) {
      categoryStats.value.forEach(cat => {
        const found = stats.value.categoryStats.find((c: any) => c.name === cat.name)
        cat.count = found?.count || 0
      })
    }
    
    weightStats.value[0].count = stats.value.smallWeight || 0
    weightStats.value[1].count = stats.value.mediumWeight || 0
    weightStats.value[2].count = stats.value.largeWeight || 0
    weightStats.value[3].count = stats.value.extraLargeWeight || 0
  } catch (error) {
    console.error(error)
  }
}

// 用户列表
const loadUserList = async () => {
  userLoading.value = true
  try {
    const res: any = await getUserList({
      pageNum: userPageNum.value,
      pageSize: userPageSize.value,
      keyword: userSearch.value || undefined,
      status: userStatusFilter.value !== '' ? userStatusFilter.value : undefined
    })
    userList.value = res.result.records || []
    userTotal.value = res.result.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    userLoading.value = false
  }
}

const toggleUserStatus = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    loadUserList()
  } catch (error) {
  }
}

const openEditUserDialog = (row: any) => {
  editUserForm.value = { ...row }
  editUserDialogVisible.value = true
}

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
  } catch (error: any) {
    ElMessage.error(error.msg || '修改失败')
  }
}

// 任务列表
const loadTaskList = async () => {
  taskLoading.value = true
  try {
    const res: any = await getAdminTaskList({
      pageNum: taskPageNum.value,
      pageSize: taskPageSize.value,
      taskNo: taskSearch.value.taskNo || undefined,
      title: taskSearch.value.title || undefined,
      status: taskSearch.value.status !== '' ? taskSearch.value.status : undefined
    })
    taskList.value = res.result.records || []
    taskTotal.value = res.result.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    taskLoading.value = false
  }
}

const resetTaskSearch = () => {
  taskSearch.value = {
    taskNo: '',
    title: '',
    status: ''
  }
  loadTaskList()
}

const openEditTaskDialog = (row: any) => {
  editTaskForm.value = { ...row }
  editTaskDialogVisible.value = true
}

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
  } catch (error: any) {
    ElMessage.error(error.msg || '修改失败')
  }
}

const handleDeleteTask = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
    await deleteTaskApi(row.id)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error) {
  }
}

// 管理员列表
const loadAdminList = async () => {
  adminLoading.value = true
  try {
    const res: any = await getAdminList({
      pageNum: adminPageNum.value,
      pageSize: adminPageSize.value,
      keyword: adminSearch.value || undefined
    })
    adminList.value = res.result.records || []
    adminTotal.value = res.result.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    adminLoading.value = false
  }
}

const openAddAdminDialog = () => {
  isAdminAdd.value = true
  adminForm.value = {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    role: 2,
    status: 1
  }
  adminDialogVisible.value = true
}

const openEditAdminDialog = (row: any) => {
  isAdminAdd.value = false
  adminForm.value = { ...row }
  adminDialogVisible.value = true
}

const handleSaveAdmin = async () => {
  try {
    if (isAdminAdd.value) {
      await addAdmin(adminForm.value)
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
    ElMessage.error(error.msg || '操作失败')
  }
}

const getTaskStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getTaskStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待接单', 1: '进行中', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}

const getWeightLevelText = (level: string) => {
  const texts: Record<string, string> = {
    small: '小件',
    medium: '中件',
    large: '大件',
    extra_large: '超大件'
  }
  return texts[level] || '小件'
}
</script>

<style scoped lang="scss">
.admin-container {
  min-height: calc(100vh - 60px);
}

.admin-submenu {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  margin: -20px -20px 20px;

  .admin-menu-horizontal {
    justify-content: center;
    border-bottom: none;
  }
}

.admin-content {
  padding: 0 20px;
}

.stat-cards {
  margin-bottom: 20px;

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 8px;
    color: #fff;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    &.total-users {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.total-tasks {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.completed-tasks {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.total-amount {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .stat-icon {
    opacity: 0.8;
    margin-right: 16px;
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 14px;
      opacity: 0.9;
      margin-top: 4px;
    }
  }

  .stat-trend {
    .trend-up {
      color: rgba(255, 255, 255, 0.9);
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.mt-20 {
  margin-top: 20px;
}

.card-header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-distribution {
  .progress-item {
    margin-bottom: 24px;

    .progress-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-weight: 500;

      .num {
        font-weight: 700;
        color: #333;
      }
    }
  }
}

.completion-rate {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

  .rate-circle {
    text-align: center;

    p {
      margin: 10px 0 0;
      color: #666;
      font-weight: 500;
    }
  }
}

.category-stats {
  .category-item {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .category-name {
      width: 100px;
      font-size: 14px;
      color: #666;
    }

    .category-bar {
      flex: 1;
      height: 24px;
      background: #f5f7fa;
      border-radius: 12px;
      position: relative;
      overflow: hidden;

      .bar-fill {
        height: 100%;
        border-radius: 12px;
        transition: width 0.5s ease;
      }

      .bar-count {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 12px;
        font-weight: 600;
        color: #333;
      }
    }
  }
}

.weight-stats {
  .weight-item {
    text-align: center;
    padding: 15px 10px;
    border: 2px solid;
    border-radius: 8px;

    .weight-label {
      font-size: 12px;
      color: #666;
      margin-bottom: 5px;
    }

    .weight-count {
      font-size: 24px;
      font-weight: 700;
      color: #333;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.reward-text {
  color: #f56c6c;
  font-weight: 600;
}

:deep(.dashboard) {
  .el-card__header {
    font-weight: 600;
  }
}
</style>
