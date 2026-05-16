<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon :size="28" class="logo-icon"><Box /></el-icon>
        <span v-show="!isCollapse" class="logo-text">校园跑腿</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :background-color="menuBgColor"
        :text-color="menuTextColor"
        :active-text-color="menuActiveColor"
        router
        class="sidebar-menu"
      >
        <template v-if="isAdminRoute">
          <!-- 管理员菜单 -->
          <el-menu-item index="/admin">
            <el-icon><DataLine /></el-icon>
            <template #title>数据概览</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/tasks">
            <el-icon><Document /></el-icon>
            <template #title>任务管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/admins">
            <el-icon><Setting /></el-icon>
            <template #title>管理员管理</template>
          </el-menu-item>

          <el-divider class="menu-divider" />

          <!-- 返回用户端 -->
          <el-menu-item index="/tasks">
            <el-icon><HomeFilled /></el-icon>
            <template #title>返回用户端</template>
          </el-menu-item>
        </template>

        <template v-else>
          <!-- 普通用户菜单 -->
          <el-menu-item index="/tasks">
            <el-icon><HomeFilled /></el-icon>
            <template #title>任务大厅</template>
          </el-menu-item>
          <el-menu-item index="/publish" v-if="userStore.currentRole === 'publisher'">
            <el-icon><Plus /></el-icon>
            <template #title>发布任务</template>
          </el-menu-item>
          <el-menu-item index="/my-tasks">
            <el-icon><Document /></el-icon>
            <template #title>我的任务</template>
          </el-menu-item>
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <template #title>我的收藏</template>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <template #title>我的订单</template>
          </el-menu-item>

          <el-divider class="menu-divider" />

          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>

          <!-- 管理员专用入口 -->
          <template v-if="isAdmin">
            <el-menu-item index="/admin">
            <el-icon><Setting /></el-icon>
            <template #title>管理员后台</template>
            </el-menu-item>
          </template>
        </template>
      </el-menu>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 信誉分警告条 -->
      <div class="credit-warning-bar" v-if="showCreditWarning" @click="goToCreditScore">
        <el-icon><Warning /></el-icon>
        <span>您的信誉分为 {{ userStore.userInfo?.creditScore }}，{{ creditWarningText }}</span>
        <span class="link">点击查看详情 <el-icon><ArrowRight /></el-icon></span>
      </div>

      <!-- 顶部导航 -->
      <el-header class="header" height="60px">
        <div class="header-left">
          <h3 class="page-title">{{ $route.meta.title || '校园跑腿' }}</h3>
        </div>
        <div class="header-right">
          <div class="role-switch" @click="toggleRole" :title="userStore.currentRole === 'publisher' ? '切换到跑腿者' : '切换到发布者'">
            <el-icon :size="20">
              <UserFilled v-if="userStore.currentRole === 'publisher'" />
              <Van v-else />
            </el-icon>
            <span class="role-text">{{ userStore.currentRole === 'publisher' ? '发布者' : '跑腿者' }}</span>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || defaultAvatar" />
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <slot>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </slot>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  Plus,
  Document,
  Star,
  Wallet,
  List,
  User,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  ArrowRight,
  SwitchButton,
  Box,
  DataLine,
  UserFilled,
  Van,
  Warning
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 根据当前路由判断是否为管理员界面
const isAdminRoute = computed(() => route.path.startsWith('/admin'))

// 是否为管理员（从 localStorage 直接读取作为备用）
const isAdmin = computed(() => {
  // 优先从 store 中获取
  if (userStore.userInfo?.role) {
    return userStore.userInfo.role === 'admin'
  }
  // 如果 store 中没有，从 localStorage 读取
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    try {
      const userInfo = JSON.parse(stored)
      return userInfo.role === 'admin'
    } catch (e) {
      return false
    }
  }
  return false
})

// 菜单主题色
const menuBgColor = computed(() => {
  if (isAdminRoute.value) return '#1a1a2e'
  return userStore.currentRole === 'publisher' ? '#304156' : '#4a3728'
})
const menuTextColor = computed(() => '#bfcbd9')
const menuActiveColor = computed(() => {
  if (isAdminRoute.value) return '#e6a23c'
  return userStore.currentRole === 'publisher' ? '#409eff' : '#e6a23c'
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 信誉分警告
const showCreditWarning = computed(() => {
  if (isAdminRoute.value) return false
  const score = userStore.userInfo?.creditScore
  return score !== undefined && score < 80 && score >= 60
})

const creditWarningText = computed(() => {
  const score = userStore.userInfo?.creditScore || 0
  if (score >= 60) return '信誉分偏低，部分功能可能受限'
  return ''
})

const goToCreditScore = () => {
  router.push('/credit-score')
}

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 切换身份角色
const toggleRole = () => {
  const newRole = userStore.currentRole === 'publisher' ? 'runner' : 'publisher'
  userStore.switchRole(newRole)
  ElMessage.success(`已切换到${newRole === 'publisher' ? '发布者' : '跑腿者'}模式`)
  
  // 如果当前在发布任务页面且切换到跑腿者,跳转到任务大厅
  if (route.path === '/publish' && newRole === 'runner') {
    router.push('/tasks')
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 取消退出
  }
}
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.sidebar {
  background-color: v-bind(menuBgColor);
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  height: 100vh;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .logo-icon {
      color: v-bind(menuActiveColor);
      flex-shrink: 0;
    }

    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      margin-left: 12px;
      white-space: nowrap;
      overflow: hidden;
    }
  }

  .sidebar-menu {
    flex: 1;
    border-right: none;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 0;
      height: 0;
    }

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;

      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }

      &.is-active {
        background-color: rgba(64, 158, 255, 0.1) !important;
      }
    }

    :deep(.el-icon) {
      font-size: 18px;
    }
  }

  .menu-divider {
    margin: 8px 16px;
    border-color: rgba(255, 255, 255, 0.1);
  }

  .collapse-btn {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #bfcbd9;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s;

    &:hover {
      background-color: rgba(255, 255, 255, 0.05);
      color: #fff;
    }

    .el-icon {
      font-size: 18px;
    }
  }
}

.main-container {
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.credit-warning-bar {
  background-color: #fdf6ec;
  border-bottom: 1px solid #f5dab1;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: background-color 0.3s;

  &:hover {
    background-color: #fcebd8;
  }

  .el-icon {
    color: #e6a23c;
  }

  span {
    color: #909399;
    font-size: 14px;
  }

  .link {
    color: #409eff;
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: 16px;

    .el-icon {
      color: #409eff;
    }
  }
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;

    .page-title {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: #303133;
    }
  }

  .header-right {
    display: flex;
    align-items: center;

    .role-switch {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 20px;
      margin-right: 16px;
      transition: all 0.3s;
      background-color: v-bind(menuActiveColor);
      color: #fff;

      &:hover {
        opacity: 0.8;
      }

      .role-text {
        margin-left: 6px;
        font-size: 14px;
        font-weight: 500;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      .username {
        margin: 0 8px;
        font-size: 14px;
        color: #606266;
      }

      .el-icon {
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.main-content {
  padding: 20px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #c0c4cc;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background-color: transparent;
  }
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
