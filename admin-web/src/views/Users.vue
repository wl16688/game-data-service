<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex flex-col sm:flex-row items-center justify-between space-y-4 sm:space-y-0">
        <span class="text-lg font-bold text-gray-800">用户管理</span>
        <div class="flex items-center space-x-2">
          <el-input placeholder="搜索昵称或ID" prefix-icon="Search" class="w-64 shadow-sm" clearable />
          <el-button type="primary" icon="Search" class="shadow-sm">查询</el-button>
        </div>
      </div>
    </template>
    
    <el-table :data="users" v-loading="loading" class="w-full rounded-lg overflow-hidden border border-gray-100" header-cell-class-name="bg-gray-50 text-gray-600 font-semibold text-sm">
      <el-table-column prop="id" label="ID" width="100" align="center" />
      <el-table-column prop="nickname" label="用户昵称" width="200">
        <template #default="{ row }">
          <div class="flex items-center space-x-3">
            <el-avatar :size="32" :src="row.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <span class="font-medium text-gray-900">{{ row.nickname || '未设置' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="openid" label="微信 OpenID" width="220" />
      <el-table-column prop="provinceId" label="省份 ID" width="120" align="center">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.provinceId || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="Edit">编辑资料</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="mt-4 flex justify-end">
      <el-pagination background layout="total, prev, pager, next" :total="2" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchUsers } from '@/api'

const users = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    users.value = await fetchUsers()
  } finally {
    loading.value = false
  }
})
</script>
