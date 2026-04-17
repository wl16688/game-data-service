<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex flex-col sm:flex-row items-center justify-between space-y-4 sm:space-y-0">
        <span class="text-lg font-bold text-gray-800">用户管理</span>
        <div class="flex items-center space-x-2">
          <el-input v-model="keyword" placeholder="搜索昵称或ID" prefix-icon="Search" class="w-64 shadow-sm" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
          <el-button type="primary" icon="Search" class="shadow-sm" @click="handleSearch">查询</el-button>
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
          <el-button type="primary" link size="small" icon="Edit" @click="openEdit(row)">编辑资料</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="mt-4 flex justify-end">
      <el-pagination 
        background 
        layout="total, prev, pager, next" 
        :total="total" 
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>
  </el-card>

  <el-drawer v-model="drawerVisible" title="编辑用户资料" size="520px" destroy-on-close>
    <el-form :model="editForm" label-width="90px">
      <el-form-item label="用户ID">
        <el-input v-model="editForm.id" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="头像URL">
        <el-input v-model="editForm.avatarUrl" placeholder="https://..." />
      </el-form-item>
      <el-form-item label="国家ID">
        <el-input-number v-model="editForm.countryId" :min="0" class="w-full" />
      </el-form-item>
      <el-form-item label="省份ID">
        <el-input-number v-model="editForm.provinceId" :min="0" class="w-full" />
      </el-form-item>
      <el-form-item label="城市ID">
        <el-input-number v-model="editForm.cityId" :min="0" class="w-full" />
      </el-form-item>
      <el-form-item label="区县ID">
        <el-input-number v-model="editForm.districtId" :min="0" class="w-full" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="flex justify-end space-x-2">
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUsers, updateUser } from '@/api'

const users = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const drawerVisible = ref(false)
const saving = ref(false)

const editForm = ref<any>({
  id: '',
  nickname: '',
  avatarUrl: '',
  countryId: null,
  provinceId: null,
  cityId: null,
  districtId: null
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await fetchUsers(keyword.value, currentPage.value, pageSize.value)
    users.value = res.content || []
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

onMounted(() => {
  loadData()
})

const openEdit = (row: any) => {
  editForm.value = {
    id: row.id,
    nickname: row.nickname || '',
    avatarUrl: row.avatarUrl || '',
    countryId: row.countryId,
    provinceId: row.provinceId,
    cityId: row.cityId,
    districtId: row.districtId
  }
  drawerVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.value.id) return
  saving.value = true
  try {
    await updateUser(editForm.value.id, editForm.value)
    ElMessage.success('保存成功')
    drawerVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}
</script>
