<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="text-lg font-bold text-gray-800">游戏列表</span>
        <el-button type="primary" class="rounded-lg shadow-sm" icon="Plus">新增游戏</el-button>
      </div>
    </template>
    
    <el-table :data="games" v-loading="loading" class="w-full rounded-lg overflow-hidden border border-gray-100" header-cell-class-name="bg-gray-50 text-gray-600 font-semibold text-sm">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="游戏名称" width="180">
        <template #default="{ row }">
          <span class="font-medium text-gray-900">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="游戏代号 (Code)" width="150" />
      <el-table-column prop="description" label="游戏描述" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="Edit">编辑</el-button>
          <el-button type="success" link size="small" icon="Setting">平台配置</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="mt-4 flex justify-end">
      <el-pagination background layout="prev, pager, next" :total="2" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchGames } from '@/api'

const games = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    games.value = await fetchGames()
  } finally {
    loading.value = false
  }
})
</script>
