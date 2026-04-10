<template>
  <div class="home-container">
    <div class="header">
      <div class="logo">
        <el-icon size="32" color="#667eea"><Van /></el-icon>
        <span>校园跑腿</span>
      </div>
      <div class="user-info" v-if="userStore.userInfo">
        <span>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>
    
    <div class="main-content">
      <h2>欢迎使用校园跑腿系统</h2>
      <p>这是一个功能完整的校园跑腿平台后端+前端项目</p>
      
      <div class="feature-cards">
        <el-card class="feature-card" @click="$router.push('/tasks')">
          <el-icon size="48" color="#667eea"><List /></el-icon>
          <h3>任务大厅</h3>
          <p>浏览所有可接的任务</p>
        </el-card>
        
        <el-card class="feature-card" @click="$router.push('/publish')">
          <el-icon size="48" color="#67c23a"><Plus /></el-icon>
          <h3>发布任务</h3>
          <p>发布你的跑腿需求</p>
        </el-card>
        
        <el-card class="feature-card" @click="$router.push('/my-tasks')">
          <el-icon size="48" color="#e6a23c"><Document /></el-icon>
          <h3>我的任务</h3>
          <p>查看我发布和接受的任务</p>
        </el-card>
        
        <el-card class="feature-card" @click="$router.push('/profile')">
          <el-icon size="48" color="#909399"><User /></el-icon>
          <h3>个人中心</h3>
          <p>管理个人信息</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van, List, Plus, Document, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, logout as logoutApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  // 获取用户信息
  try {
    const res: any = await getUserInfo()
    userStore.setUserInfo(res.result)
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
})

const handleLogout = async () => {
  try {
    await logoutApi()
  } catch (error) {
    // 即使后端调用失败，也要清除本地登录状态
    console.error('后端退出失败', error)
  } finally {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.main-content {
  padding: 40px;
  text-align: center;
  
  h2 {
    font-size: 32px;
    color: #333;
    margin-bottom: 16px;
  }
  
  > p {
    color: #666;
    margin-bottom: 48px;
  }
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.feature-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
  
  h3 {
    margin: 16px 0 8px;
    color: #333;
  }
  
  p {
    color: #999;
    font-size: 14px;
  }
}
</style>
