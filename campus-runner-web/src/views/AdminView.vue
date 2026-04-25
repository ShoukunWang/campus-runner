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
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-20">
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="h-420">
              <template #header>
                <div class="card-header-title">
                  <span>任务状态分布</span>
                  <el-button type="primary" link size="small" @click="loadStats">刷新</el-button>
                </div>
              </template>
              <div class="status-distribution">
                <div class="progress-item">
                  <div class="progress-label">
                    <span>待接取</span>
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
                    <span>进行中/已提货</span>
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
                    <span>待确认</span>
                    <span class="num">{{ stats.confirmingTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.confirmingTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#409eff"
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
                    <span>已取消/已退回</span>
                    <span class="num">{{ stats.cancelledTasks || 0 }} 单</span>
                  </div>
                  <el-progress 
                    :percentage="Math.round((stats.cancelledTasks || 0) / (stats.totalTasks || 1) * 100)" 
                    :stroke-width="16"
                    color="#f56c6c"
                  />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="hover" class="h-420">
              <template #header>
                <span>平台运营摘要</span>
              </template>
              <div class="platform-summary">
                <div class="summary-item">
                  <div class="summary-label">用户活跃度</div>
                  <div class="summary-value">
                    <span class="main">98.5%</span>
                    <span class="tip">正常运营中</span>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">任务完成率</div>
                  <div class="summary-value">
                    <span class="main">{{ taskCompletionRate }}%</span>
                    <span class="tip">{{ taskCompletionRate >= 90 ? '优秀' : taskCompletionRate >= 70 ? '良好' : '需提升' }}</span>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">平均任务完成时长</div>
                  <div class="summary-value">
                    <span class="main">45</span>
                    <span class="tip">分钟</span>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">用户平均信用分</div>
                  <div class="summary-value">
                    <span class="main">92</span>
                    <span class="tip">良好</span>
                  </div>
                </div>
              </div>
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
            </div>
          </template>
          <div class="search-bar">
            <el-form :inline="true" :model="userSearchForm">
              <el-form-item label="搜索">
                <el-input
                  v-model="userSearchForm.keyword"
                  placeholder="昵称/手机号"
                  style="width: 200px"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUserSearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleUserSearchReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="userList" style="width: 100%" v-loading="userLoading">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" width="120" />
            <el-table-column prop="phone" label="手机号" width="130" />
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
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button
                  size="small"
                  :type="scope.row.status === 1 ? 'danger' : 'success'"
                  @click="handleUserStatus(scope.row)"
                >
                  {{ scope.row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" @click="handleEditUser(scope.row)">编辑</el-button>
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
            <span>任务列表</span>
          </template>
          <div class="search-bar">
            <el-form :inline="true" :model="taskSearchForm">
              <el-form-item label="任务编号">
                <el-input
                  v-model="taskSearchForm.taskNo"
                  placeholder="请输入任务编号"
                  style="width: 180px"
                  clearable
                />
              </el-form-item>
              <el-form-item label="任务标题">
                <el-input
                  v-model="taskSearchForm.title"
                  placeholder="请输入任务标题"
                  style="width: 180px"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleTaskSearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleTaskSearchReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="taskList" style="width: 100%" v-loading="taskLoading">
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
            <el-table-column label="操作" width="220" fixed="right" align="center">
              <template #default="scope">
                <el-button
                  size="small"
                  @click="handleEditTaskStatus(scope.row)"
                  :disabled="scope.row.status === 4 || scope.row.status === 5"
                >
                  修改状态
                </el-button>
                <el-button size="small" type="danger" @click="handleDeleteTask(scope.row)">删除</el-button>
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
              <el-button type="primary" size="small" @click="handleAddAdmin">
                <el-icon><Plus /></el-icon>
                添加管理员
              </el-button>
            </div>
          </template>
          <div class="search-bar">
            <el-form :inline="true" :model="adminSearchForm">
              <el-form-item label="搜索">
                <el-input
                  v-model="adminSearchForm.keyword"
                  placeholder="真实姓名/手机号"
                  style="width: 200px"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleAdminSearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleAdminSearchReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="adminList" style="width: 100%" v-loading="adminLoading">
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button
                  size="small"
                  :type="scope.row.status === 1 ? 'danger' : 'success'"
                  @click="handleAdminStatus(scope.row)"
                >
                  {{ scope.row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" @click="handleEditAdmin(scope.row)">编辑</el-button>
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

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="userDialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="userForm.creditScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 管理员弹窗 -->
    <el-dialog v-model="adminDialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="adminForm" label-width="100px">
        <el-form-item label="用户名" v-if="!isEdit">
          <el-input v-model="adminForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="adminForm.password" type="password" :placeholder="isEdit ? '不修改请留空' : ''" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="adminForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="adminForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="adminForm.role">
            <el-radio :label="1">超级管理员</el-radio>
            <el-radio :label="2">普通管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAdmin">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务状态修改弹窗 -->
    <el-dialog v-model="taskStatusDialogVisible" title="管理员干预 - 修改任务状态" width="500px">
      <el-alert
        title="注意：此操作将直接修改任务状态，可能影响用户权益，请谨慎操作！"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="taskStatusForm" label-width="120px">
        <el-form-item label="当前状态">
          <el-tag :type="getTaskStatusType(taskStatusForm.currentStatus)">
            {{ getTaskStatusText(taskStatusForm.currentStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="修改为">
          <el-select v-model="taskStatusForm.targetStatus" style="width: 100%" placeholder="请选择目标状态">
            <el-option
              v-for="item in availableStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="干预原因">
          <el-input
            v-model="taskStatusForm.cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请详细说明进行状态干预的原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTaskStatus">确认执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DataLine, User, List, Setting, Document, 
  UserFilled, CircleCheck, Money, Plus, Search
} from '@element-plus/icons-vue'
import { 
  getStatistics, 
  getUserList, 
  getAdminTaskList, 
  getAdminList,
  updateUserStatus,
  updateUserInfo,
  updateAdminTask,
  deleteAdminTask,
  addAdmin,
  updateAdmin
} from '@/api/admin'

const route = useRoute()

const stats = ref<any>({})

// 用户管理
const userLoading = ref(false)
const userList = ref<any[]>([])
const userTotal = ref(0)
const userPageNum = ref(1)
const userPageSize = ref(10)
const userSearchForm = ref({
  keyword: ''
})

// 任务管理
const taskLoading = ref(false)
const taskList = ref<any[]>([])
const taskTotal = ref(0)
const taskPageNum = ref(1)
const taskPageSize = ref(10)
const taskSearchForm = ref({
  taskNo: '',
  title: ''
})

// 管理员管理
const adminLoading = ref(false)
const adminList = ref<any[]>([])
const adminTotal = ref(0)
const adminPageNum = ref(1)
const adminPageSize = ref(10)
const adminSearchForm = ref({
  keyword: ''
})

// 弹窗
const userDialogVisible = ref(false)
const userForm = ref({
  id: '',
  nickname: '',
  phone: '',
  creditScore: 100
})

const adminDialogVisible = ref(false)
const adminForm = ref({
  id: '',
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 2
})

// 任务状态修改
const taskStatusDialogVisible = ref(false)
const taskStatusForm = ref({
  id: '',
  currentStatus: 0,
  targetStatus: 0,
  cancelReason: ''
})

const availableStatusOptions = ref<any[]>([])

const dialogTitle = ref('')
const isEdit = ref(false)

const activeMenu = computed(() => route.path)

const taskCompletionRate = computed(() => {
  const total = stats.value.totalTasks || 0
  const completed = stats.value.completedTasks || 0
  return total > 0 ? Math.round(completed / total * 100) : 0
})

const getTaskStatusType = (status: number) => {
  const s = String(status)
  if (s === '4') return 'success'
  if (s === '3') return 'primary'
  if (s === '1' || s === '2') return 'warning'
  if (s === '6') return 'danger'
  return 'info'
}

const getTaskStatusText = (status: number) => {
  const s = String(status)
  if (s === '-1') return '待发布'
  if (s === '0') return '待接取'
  if (s === '1') return '进行中'
  if (s === '2') return '已提货'
  if (s === '3') return '待确认'
  if (s === '4') return '已完成'
  if (s === '5') return '已取消'
  if (s === '6') return '已退回'
  return '未知'
}

const loadStats = async () => {
  try {
    const res: any = await getStatistics()
    stats.value = res.result || {}
    console.log('统计数据加载成功:', stats.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('加载统计数据失败')
  }
}

const loadUserList = async () => {
  userLoading.value = true
  try {
    console.log('开始加载用户列表，搜索条件:', userSearchForm.value)
    const res: any = await getUserList({
      pageNum: userPageNum.value,
      pageSize: userPageSize.value,
      keyword: userSearchForm.value.keyword
    })
    console.log('用户列表响应:', res)
    userList.value = res.result.records || []
    userTotal.value = res.result.total || 0
    console.log(`用户列表加载成功，共${userTotal.value}条`)
  } catch (error) {
    console.error('用户列表加载失败:', error)
  } finally {
    userLoading.value = false
  }
}

const loadTaskList = async () => {
  taskLoading.value = true
  try {
    console.log('开始加载任务列表，搜索条件:', taskSearchForm.value)
    const res: any = await getAdminTaskList({
      pageNum: taskPageNum.value,
      pageSize: taskPageSize.value,
      taskNo: taskSearchForm.value.taskNo,
      title: taskSearchForm.value.title
    })
    console.log('任务列表响应:', res)
    taskList.value = res.result.records || []
    taskTotal.value = res.result.total || 0
    console.log(`任务列表加载成功，共${taskTotal.value}条`)
  } catch (error) {
    console.error('任务列表加载失败:', error)
  } finally {
    taskLoading.value = false
  }
}

const loadAdminList = async () => {
  adminLoading.value = true
  try {
    console.log('开始加载管理员列表，搜索条件:', adminSearchForm.value)
    const res: any = await getAdminList({
      pageNum: adminPageNum.value,
      pageSize: adminPageSize.value,
      keyword: adminSearchForm.value.keyword
    })
    console.log('管理员列表响应:', res)
    adminList.value = res.result.records || []
    adminTotal.value = res.result.total || 0
    console.log(`管理员列表加载成功，共${adminTotal.value}条`)
  } catch (error) {
    console.error('管理员列表加载失败:', error)
  } finally {
    adminLoading.value = false
  }
}

const handleUserSearch = () => {
  userPageNum.value = 1
  loadUserList()
}

const handleUserSearchReset = () => {
  userSearchForm.value.keyword = ''
  userPageNum.value = 1
  loadUserList()
}

const handleTaskSearch = () => {
  taskPageNum.value = 1
  loadTaskList()
}

const handleTaskSearchReset = () => {
  taskSearchForm.value.taskNo = ''
  taskSearchForm.value.title = ''
  taskPageNum.value = 1
  loadTaskList()
}

const handleAdminSearch = () => {
  adminPageNum.value = 1
  loadAdminList()
}

const handleAdminSearchReset = () => {
  adminSearchForm.value.keyword = ''
  adminPageNum.value = 1
  loadAdminList()
}

// ==================== 用户操作 ====================
const handleUserStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
    loadUserList()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const handleEditUser = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  userForm.value = {
    id: row.id,
    nickname: row.nickname,
    phone: row.phone,
    creditScore: row.creditScore
  }
  userDialogVisible.value = true
}

const saveUser = async () => {
  try {
    await updateUserInfo(userForm.value.id, {
      nickname: userForm.value.nickname,
      phone: userForm.value.phone,
      creditScore: userForm.value.creditScore
    })
    ElMessage.success('保存成功')
    userDialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

// ==================== 任务操作 ====================
const handleEditTaskStatus = (row: any) => {
  taskStatusForm.value = {
    id: row.id,
    currentStatus: row.status,
    targetStatus: row.status,
    cancelReason: ''
  }
  
  const options: any = {
    '0': [{ value: 6, label: '已退回（强制下架）' }],
    '1': [{ value: 6, label: '已退回（取消任务）' }],
    '2': [{ value: 6, label: '已退回（取消任务）' }],
    '3': [
      { value: 4, label: '已完成（判定完成）' },
      { value: 6, label: '已退回（判定退回）' }
    ],
    '6': [{ value: 0, label: '待接取（重新上架）' }]
  }
  
  availableStatusOptions.value = options[String(row.status)] || []
  taskStatusDialogVisible.value = true
}

const saveTaskStatus = async () => {
  if (!taskStatusForm.value.cancelReason.trim()) {
    ElMessage.warning('请填写干预原因')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定将任务状态从「${getTaskStatusText(taskStatusForm.value.currentStatus)}」改为「${getTaskStatusText(taskStatusForm.value.targetStatus)}」吗？`,
      '管理员干预确认',
      {
        confirmButtonText: '确定执行',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateAdminTask(taskStatusForm.value.id, {
      status: taskStatusForm.value.targetStatus,
      cancelReason: `【管理员干预】${taskStatusForm.value.cancelReason}`
    })
    
    ElMessage.success('状态修改成功')
    taskStatusDialogVisible.value = false
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('修改失败')
    }
  }
}

const handleDeleteTask = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAdminTask(row.id)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

// ==================== 管理员操作 ====================
const handleAdminStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateAdmin(row.id, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '管理员已启用' : '管理员已禁用')
    loadAdminList()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const handleEditAdmin = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑管理员'
  adminForm.value = {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone,
    role: row.role
  }
  adminDialogVisible.value = true
}

const handleAddAdmin = () => {
  isEdit.value = false
  dialogTitle.value = '添加管理员'
  adminForm.value = {
    id: '',
    username: '',
    password: '',
    realName: '',
    phone: '',
    role: 2
  }
  adminDialogVisible.value = true
}

const saveAdmin = async () => {
  try {
    if (isEdit.value) {
      const data: any = {
        realName: adminForm.value.realName,
        phone: adminForm.value.phone,
        role: adminForm.value.role
      }
      if (adminForm.value.password) {
        data.password = adminForm.value.password
      }
      await updateAdmin(adminForm.value.id, data)
    } else {
      await addAdmin(adminForm.value)
    }
    ElMessage.success('保存成功')
    adminDialogVisible.value = false
    loadAdminList()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

watch(
  () => route.path,
  (newPath) => {
    console.log('路由切换到:', newPath)
    if (newPath === '/admin') {
      loadStats()
    } else if (newPath === '/admin/users') {
      loadUserList()
    } else if (newPath === '/admin/tasks') {
      loadTaskList()
    } else if (newPath === '/admin/admins') {
      loadAdminList()
    }
  },
  { immediate: true }
)

onMounted(() => {
  console.log('✅ AdminView挂载成功！当前路径:', route.path)
})
</script>

<style scoped lang="scss">
.admin-container {
  min-height: calc(100vh - 60px);

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

  .table-pagination {
    margin-top: 20px;
    text-align: right;
  }

  .stat-cards {
    margin-bottom: 20px;

    .stat-card {
      padding: 20px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      color: #fff;

      .stat-icon {
        margin-right: 16px;
        opacity: 0.8;
      }

      .stat-content {
        .stat-value {
          font-size: 28px;
          font-weight: 600;
          line-height: 1.2;
        }
        .stat-label {
          font-size: 14px;
          opacity: 0.8;
          margin-top: 4px;
        }
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

      &:last-child {
        margin-bottom: 0;
      }

      .progress-label {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;

        .num {
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-bar {
    padding: 10px 0;
    background: #f5f7fa;
    padding: 15px 20px;
    margin-bottom: 10px;
    border-radius: 4px;

    .el-form {
      margin-bottom: 0;
    }

    .el-form-item {
      margin-bottom: 0;
    }
  }

  .h-420 {
    height: 420px;
  }

  .platform-summary {
    padding: 10px 0;

    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px dashed #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .summary-label {
        font-size: 14px;
        color: #606266;
      }

      .summary-value {
        display: flex;
        align-items: baseline;
        gap: 8px;

        .main {
          font-size: 24px;
          font-weight: 600;
          color: #409eff;
        }

        .tip {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>
