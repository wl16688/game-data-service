<template>
  <div class="flex h-screen w-full bg-gray-50">
    <el-aside width="240px" class="bg-slate-900 text-white flex flex-col transition-all duration-300">
      <div class="h-16 flex items-center justify-center font-bold text-xl tracking-wider border-b border-slate-800 shadow-sm">
        Game Admin
      </div>
      <el-menu
        :default-active="route.path"
        class="flex-1 border-r-0"
        background-color="#0f172a"
        text-color="#94a3b8"
        active-text-color="#fff"
        router
      >
        <el-menu-item
          v-for="item in menuRoutes"
          :key="item.path"
          :index="'/' + item.path"
          class="hover:bg-slate-800"
        >
          <el-icon><component :is="item.meta?.icon" /></el-icon>
          <span>{{ item.meta?.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="flex flex-col flex-1 overflow-hidden">
      <el-header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shadow-sm">
        <div class="font-medium text-gray-700">
          {{ currentTitle }}
        </div>
        <div class="flex items-center space-x-4">
          <el-dropdown @command="handleCommand">
            <span class="flex items-center cursor-pointer text-gray-600 hover:text-blue-600 transition-colors">
              <el-avatar :size="32" class="mr-2 bg-blue-500">A</el-avatar>
              Admin
              <el-icon class="ml-1"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="p-6 bg-gray-50 overflow-y-auto">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuRoutes = computed(() => {
  const rootRoute = router.options.routes.find(r => r.path === '/')
  return rootRoute?.children?.filter(child => child.meta && child.meta.title) || []
})

const currentTitle = computed(() => route.meta.title as string)

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.el-menu {
  border-right: none;
}
.el-menu-item.is-active {
  background-color: #1e293b !important;
  border-right: 3px solid #3b82f6;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
