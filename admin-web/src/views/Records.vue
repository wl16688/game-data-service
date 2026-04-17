<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex flex-col sm:flex-row items-center justify-between space-y-4 sm:space-y-0">
        <span class="text-lg font-bold text-gray-800">通关流水</span>
        <div class="flex items-center space-x-2">
          <el-input v-model="userId" placeholder="按用户ID筛选" class="shadow-sm w-48" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
          <el-button type="primary" icon="Search" class="shadow-sm" @click="handleSearch">筛选</el-button>
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
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row.id)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="mt-4 flex justify-end">
      <el-pagination 
        background 
        layout="total, sizes, prev, pager, next, jumper" 
        :total="total" 
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchRecords, deleteRecord } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref<any[]>([])
const loading = ref(false)
const userId = ref('')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await fetchRecords(userId.value, currentPage.value, pageSize.value)
    records.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要作废这条通关记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRecord(id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>
