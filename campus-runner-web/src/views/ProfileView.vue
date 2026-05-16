<template>
  <div class="profile-container">
    <el-card class="profile-card" v-loading="loading">
      <template v-if="userInfo">
        <div class="profile-header">
          <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" />
          <div class="user-meta">
            <h3>{{ userInfo.nickname || userInfo.username }}</h3>
            <p>信用积分: {{ userInfo.creditScore }}</p>
          </div>
        </div>
        
        <el-descriptions :column="1" border class="info-list">
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ getRoleText(userInfo.role) }}</el-descriptions-item>
          <el-descriptions-item label="完成率">{{ userInfo.completionRate }}%</el-descriptions-item>
        </el-descriptions>
        
        <div class="menu-grid">
          <div class="menu-item" @click="$router.push('/my-tasks')">
            <el-icon><Document /></el-icon>
            <span>我的任务</span>
          </div>
          <div class="menu-item" @click="$router.push('/favorites')">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </div>
          <div class="menu-item" @click="$router.push('/orders')">
            <el-icon><Wallet /></el-icon>
            <span>我的订单</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" @click="dialogVisible = true">编辑资料</el-button>
        </div>
      </template>
    </el-card>
    
    <!-- 编辑资料对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Star, Wallet } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { getCurrentAdmin } from '@/api/admin'

const userStore = useUserStore()
const loading = ref(false)
const dialogVisible = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = ref<any>(null)
const editForm = ref({
  nickname: '',
  phone: '',
  email: ''
})

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  loading.value = true
  try {
    let res: any
    // 判断是否为管理员
    const stored = localStorage.getItem('userInfo')
    const isAdminUser = stored ? JSON.parse(stored).role === 'admin' : false

    if (isAdminUser) {
      // 管理员调用管理员接口
      res = await getCurrentAdmin()
      // 将管理员数据转换为统一格式
      const adminData = {
        id: res.result.id,
        username: res.result.username,
        nickname: res.result.realName || res.result.username,
        role: 'admin',
        creditScore: 100,
        completionRate: 100,
        phone: '-',
        email: res.result.email || '-'
      }
      userInfo.value = adminData
      userStore.setUserInfo(adminData)

      // 初始化编辑表单
      editForm.value = {
        nickname: adminData.nickname || '',
        phone: '-',
        email: res.result.email || ''
      }
    } else {
      // 普通用户调用用户接口
      res = await getUserInfo()
      userInfo.value = res.result
      userStore.setUserInfo(res.result)

      // 初始化编辑表单
      editForm.value = {
        nickname: res.result.nickname || '',
        phone: res.result.phone || '',
        email: res.result.email || ''
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getRoleText = (role: number | string) => {
  if (role === 'admin') return '管理员'
  const roles: Record<number, string> = { 0: '发布者', 1: '跑腿员', 2: '两者皆可' }
  return roles[role as number] || '未知'
}

const handleSave = async () => {
  try {
    await updateUserInfo(editForm.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadUserInfo()
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped lang="scss">
.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  
  .user-meta {
    h3 {
      margin: 0 0 8px;
      font-size: 20px;
    }
    p {
      margin: 0;
      color: #666;
    }
  }
}

.info-list {
  margin-bottom: 24px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .menu-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background-color: #e4e7ed;
      transform: translateY(-2px);
    }

    .el-icon {
      font-size: 24px;
      color: #409eff;
      margin-bottom: 8px;
    }

    span {
      font-size: 14px;
      color: #606266;
    }
  }
}

.actions {
  text-align: center;
}
</style>
