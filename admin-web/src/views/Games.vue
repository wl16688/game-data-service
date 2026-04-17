<template>
  <el-card shadow="never" class="rounded-xl border-none">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="text-lg font-bold text-gray-800">游戏列表</span>
        <el-button type="primary" class="rounded-lg shadow-sm" icon="Plus" @click="openCreateGame">新增游戏</el-button>
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
          <el-button type="primary" link size="small" icon="Edit" @click="openEditGame(row)">编辑</el-button>
          <el-button type="success" link size="small" icon="Setting" @click="openPlatforms(row)">平台配置</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="confirmDeleteGame(row)">删除</el-button>
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

  <el-dialog v-model="gameDialogVisible" :title="gameDialogTitle" width="520px" destroy-on-close>
    <el-form :model="gameForm" label-width="90px">
      <el-form-item label="游戏名称">
        <el-input v-model="gameForm.name" placeholder="请输入游戏名称" />
      </el-form-item>
      <el-form-item label="游戏代号">
        <el-input v-model="gameForm.code" placeholder="请输入唯一代号，如 sheep_game" />
      </el-form-item>
      <el-form-item label="图标URL">
        <el-input v-model="gameForm.iconUrl" placeholder="https://..." />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="gameForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="gameDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="savingGame" @click="submitGame">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="platformDrawerVisible" title="平台配置" size="520px" destroy-on-close>
    <div class="flex items-center justify-between mb-4">
      <div class="text-sm text-gray-500">游戏：<span class="text-gray-800 font-medium">{{ currentGame?.name }}</span></div>
      <el-button type="primary" icon="Plus" @click="openCreatePlatform">新增平台</el-button>
    </div>

    <el-table :data="platforms" v-loading="platformLoading" class="w-full rounded-lg overflow-hidden border border-gray-100" header-cell-class-name="bg-gray-50 text-gray-600 font-semibold text-sm">
      <el-table-column prop="platform" label="平台" width="120" />
      <el-table-column prop="versionName" label="版本号" width="120" />
      <el-table-column prop="players" label="玩家数" width="100" align="center" />
      <el-table-column prop="downloadUrl" label="下载链接" />
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="Edit" @click="openEditPlatform(row)">编辑</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="confirmDeletePlatform(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-drawer>

  <el-dialog v-model="platformDialogVisible" :title="platformDialogTitle" width="520px" destroy-on-close>
    <el-form :model="platformForm" label-width="90px">
      <el-form-item label="平台标识">
        <el-input v-model="platformForm.platform" placeholder="ios / android / web" />
      </el-form-item>
      <el-form-item label="版本号">
        <el-input v-model="platformForm.versionName" placeholder="如 1.0.0" />
      </el-form-item>
      <el-form-item label="下载链接">
        <el-input v-model="platformForm.downloadUrl" placeholder="https://..." />
      </el-form-item>
      <el-form-item label="玩家数">
        <el-input-number v-model="platformForm.players" :min="0" class="w-full" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="platformDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="savingPlatform" @click="submitPlatform">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createGame, createGamePlatform, deleteGame, deleteGamePlatform, fetchGamePlatforms, fetchGames, updateGame, updateGamePlatform } from '@/api'

const games = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const gameDialogVisible = ref(false)
const savingGame = ref(false)
const editingGameId = ref<number | null>(null)
const gameForm = ref({
  name: '',
  code: '',
  iconUrl: '',
  description: ''
})

const gameDialogTitle = computed(() => (editingGameId.value ? '编辑游戏' : '新增游戏'))

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await fetchGames(currentPage.value, pageSize.value)
    games.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const openCreateGame = () => {
  editingGameId.value = null
  gameForm.value = { name: '', code: '', iconUrl: '', description: '' }
  gameDialogVisible.value = true
}

const openEditGame = (row: any) => {
  editingGameId.value = row.id
  gameForm.value = {
    name: row.name || '',
    code: row.code || '',
    iconUrl: row.iconUrl || '',
    description: row.description || ''
  }
  gameDialogVisible.value = true
}

const submitGame = async () => {
  if (!gameForm.value.name || !gameForm.value.code) {
    ElMessage.warning('请填写游戏名称和代号')
    return
  }
  savingGame.value = true
  try {
    if (editingGameId.value) {
      await updateGame(editingGameId.value, gameForm.value)
      ElMessage.success('更新成功')
    } else {
      await createGame(gameForm.value)
      ElMessage.success('新增成功')
    }
    gameDialogVisible.value = false
    loadData()
  } finally {
    savingGame.value = false
  }
}

const confirmDeleteGame = (row: any) => {
  ElMessageBox.confirm(`确定删除游戏「${row.name}」吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteGame(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const platformDrawerVisible = ref(false)
const platformDialogVisible = ref(false)
const platformLoading = ref(false)
const savingPlatform = ref(false)
const currentGame = ref<any | null>(null)
const platforms = ref<any[]>([])
const editingPlatformId = ref<number | null>(null)
const platformForm = ref({
  platform: '',
  versionName: '',
  downloadUrl: '',
  players: 0
})

const platformDialogTitle = computed(() => (editingPlatformId.value ? '编辑平台' : '新增平台'))

const openPlatforms = async (row: any) => {
  currentGame.value = row
  platformDrawerVisible.value = true
  await loadPlatforms()
}

const loadPlatforms = async () => {
  if (!currentGame.value) return
  platformLoading.value = true
  try {
    platforms.value = await fetchGamePlatforms(currentGame.value.id)
  } finally {
    platformLoading.value = false
  }
}

const openCreatePlatform = () => {
  editingPlatformId.value = null
  platformForm.value = { platform: '', versionName: '', downloadUrl: '', players: 0 }
  platformDialogVisible.value = true
}

const openEditPlatform = (row: any) => {
  editingPlatformId.value = row.id
  platformForm.value = {
    platform: row.platform || '',
    versionName: row.versionName || '',
    downloadUrl: row.downloadUrl || '',
    players: row.players || 0
  }
  platformDialogVisible.value = true
}

const submitPlatform = async () => {
  if (!currentGame.value) return
  if (!platformForm.value.platform) {
    ElMessage.warning('请填写平台标识')
    return
  }
  savingPlatform.value = true
  try {
    if (editingPlatformId.value) {
      await updateGamePlatform(currentGame.value.id, editingPlatformId.value, platformForm.value)
      ElMessage.success('更新成功')
    } else {
      await createGamePlatform(currentGame.value.id, platformForm.value)
      ElMessage.success('新增成功')
    }
    platformDialogVisible.value = false
    await loadPlatforms()
  } finally {
    savingPlatform.value = false
  }
}

const confirmDeletePlatform = (row: any) => {
  if (!currentGame.value) return
  ElMessageBox.confirm(`确定删除平台「${row.platform}」吗？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteGamePlatform(currentGame.value.id, row.id)
    ElMessage.success('删除成功')
    await loadPlatforms()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>
