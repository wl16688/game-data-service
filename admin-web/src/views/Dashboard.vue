<template>
  <div class="space-y-6">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="rounded-xl border-none">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500">{{ item.title }}</p>
              <p class="text-3xl font-bold text-gray-900 mt-2">{{ item.value }}</p>
            </div>
            <div :class="`p-4 rounded-full ${item.bgClass} text-white`">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="rounded-xl border-none">
      <template #header>
        <div class="font-semibold text-lg text-gray-800">平台动态概览</div>
      </template>
      <div class="h-64 flex items-center justify-center bg-gray-50 rounded-lg text-gray-400 border border-dashed border-gray-200">
        图表区域 (按天通关折线图占位)
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const stats = ref([
  { title: '注册用户总数', value: '1,248', icon: 'User', bgClass: 'bg-blue-500' },
  { title: '今日通关次数', value: '3,842', icon: 'DataLine', bgClass: 'bg-emerald-500' },
  { title: '接入游戏数量', value: '2', icon: 'Trophy', bgClass: 'bg-purple-500' },
  { title: '活跃省份数量', value: '34', icon: 'MapLocation', bgClass: 'bg-amber-500' },
])
</script>
