<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="brand-section">
        <el-icon :size="64" class="brand-icon"><Box /></el-icon>
        <h1 class="brand-title">校园跑腿</h1>
        <p class="brand-slogan">让校园生活更便捷</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>快速发布任务</span>
        </div>
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>智能匹配跑腿员</span>
        </div>
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>安全便捷支付</span>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请使用您的账号密码登录系统</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item>
            <el-radio-group v-model="form.userType" size="large" class="user-type-group">
              <el-radio-button value="user">用户登录</el-radio-button>
              <el-radio-button value="admin">管理员登录</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              :placeholder="form.userType === 'admin' ? '请输入管理员账号' : '请输入用户名'"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Box, Check } from '@element-plus/icons-vue'
import { login, adminLogin } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  userType: 'user' as 'user' | 'admin'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        let res: any
        if (form.userType === 'admin') {
          res = await adminLogin({
            username: form.username,
            password: form.password
          })
        } else {
          res = await login(form)
        }
        
        userStore.setToken(res.result.token)
        
        // 保存用户信息
        userStore.setUserInfo({
          id: res.result.userId || res.result.adminId,
          username: res.result.username,
          nickname: res.result.nickname || res.result.username,
          role: form.userType === 'admin' ? 'admin' : res.result.role,
          creditScore: res.result.creditScore || 100,
          completionRate: res.result.completionRate || 100
        })
        
        ElMessage.success('登录成功')
        
        // 管理员跳转到管理员后台
        if (form.userType === 'admin') {
          router.push('/admin')
        } else {
          router.push('/')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  min-width: 100vw;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

// 左侧装饰区域
.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);

  .brand-section {
    text-align: center;
    margin-bottom: 60px;

    .brand-icon {
      color: #fff;
      margin-bottom: 24px;
    }

    .brand-title {
      font-size: 48px;
      font-weight: 700;
      margin-bottom: 16px;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }

    .brand-slogan {
      font-size: 20px;
      opacity: 0.9;
    }
  }

  .features {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 18px;
      opacity: 0.9;

      .el-icon {
        font-size: 20px;
        color: #67c23a;
      }
    }
  }
}

// 右侧登录表单
.login-right {
  width: 480px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: #fff;
}

.login-box {
  width: 100%;
  max-width: 360px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  h2 {
    font-size: 28px;
    color: #333;
    margin-bottom: 8px;
    font-weight: 600;
  }

  p {
    color: #999;
    font-size: 14px;
  }
}

.login-form {
  .el-input {
    --el-input-height: 48px;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;

  .register-link {
    color: #667eea;
    margin-left: 4px;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

.user-type-group {
  width: 100%;
  display: flex;
  justify-content: center;

  :deep(.el-radio-button__inner) {
    width: 120px;
  }
}

// 响应式适配
@media screen and (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
    flex: 1;
    padding: 20px;
  }
}
</style>
