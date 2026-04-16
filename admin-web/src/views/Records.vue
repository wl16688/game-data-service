<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex flex-col sm:flex-row items-center justify-between space-y-4 sm:space-y-0">
        <span class="text-lg font-bold text-gray-800">通关流水</span>
        <div class="flex items-center space-x-2">
          <el-date-picker
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="shadow-sm"
          />
          <el-button type="primary" icon="Filter" class="shadow-sm">筛选</el-button>
        </div>
      </div>
    </template>
    
    <el-table :data="records" v-loading="loading" class="w-full rounded-lg overflow-hidden border border-gray-100" header-cell-class-name="bg-gray-50 text-gray-600 font-semibold text-sm">
      <el-table-column prop="id" label="记录 ID" width="100" align="center" />
      <el-table-column prop="userId" label="用户 ID" width="120" align="center" />
      <el-table-column prop="gameId" label="游戏 ID" width="120" align="center" />
      <el-table-column prop="levelId" label="通关关卡" width="150" align="center">
        <template #default="{ row }">
          <el-tag type="success" size="small">{{ row.levelId }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="timestamp" label="通关时间 (客户端)" width="200" align="center">
        <template #default="{ row }">
          <span class="text-gray-500 text-sm">{{ new Date(row.timestamp).toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link size="small" icon="Delete">作废</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="mt-4 flex justify-end">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="2" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchRecords } from '@/api'

const records = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    records.value = await fetchRecords()
  } finally {
    loading.value = false
  }
})
</script>
