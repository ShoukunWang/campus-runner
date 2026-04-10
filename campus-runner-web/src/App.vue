<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AppLayout from '@/components/AppLayout.vue'

const userStore = useUserStore()
const route = useRoute()

onMounted(() => {
  // 初始化时从localStorage恢复用户信息
  userStore.initUserInfo()
})

// 判断是否需要显示布局（登录页和注册页不需要）
const needLayout = computed(() => {
  const noLayoutPaths = ['/login', '/register']
  return !noLayoutPaths.includes(route.path)
})
</script>

<template>
  <AppLayout v-if="needLayout">
    <RouterView />
  </AppLayout>
  <RouterView v-else />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
