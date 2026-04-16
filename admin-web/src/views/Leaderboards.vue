<template>
  <div class="space-y-6">
    <el-card shadow="never" class="rounded-xl border-none">
      <template #header>
        <span class="text-lg font-bold text-gray-800 flex items-center">
          <el-icon class="mr-2 text-red-500"><WarningFilled /></el-icon>
          数据运维中心
        </span>
      </template>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- 容灾恢复 -->
        <div class="p-6 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center space-x-3 mb-4">
            <div class="p-3 bg-red-50 rounded-lg text-red-600">
              <el-icon :size="24"><DataLine /></el-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-800">Redis 容灾重建</h3>
          </div>
          <p class="text-sm text-gray-500 mb-6 leading-relaxed">
            当 Redis 宕机或数据丢失时，依据 MySQL 的历史通关流水，全量重新计算并写入 Redis 榜单（支持日/周/月/总）。
          </p>
          <div class="flex items-center space-x-3">
            <el-input v-model="recoveryGameId" placeholder="输入 Game ID" class="w-32" />
            <el-button type="danger" @click="handleRecovery" :loading="recoveryLoading" class="shadow-sm">触发恢复</el-button>
          </div>
        </div>

        <!-- 强制分数干预 -->
        <div class="p-6 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center space-x-3 mb-4">
            <div class="p-3 bg-amber-50 rounded-lg text-amber-600">
              <el-icon :size="24"><EditPen /></el-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-800">强制分数干预</h3>
          </div>
          <p class="text-sm text-gray-500 mb-6 leading-relaxed">
            针对特定用户的异常分数，或者运营活动的需要，直接改写指定 Redis ZSet Key 中的绝对分数。
          </p>
          <el-button type="warning" plain @click="forceScoreDialog = true" class="shadow-sm w-full">手工改分面板</el-button>
        </div>

        <!-- 手动同步测试 -->
        <div class="p-6 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center space-x-3 mb-4">
            <div class="p-3 bg-blue-50 rounded-lg text-blue-600">
              <el-icon :size="24"><RefreshRight /></el-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-800">手工上报模拟</h3>
          </div>
          <p class="text-sm text-gray-500 mb-6 leading-relaxed">
            模拟客户端 Kafka 或 HTTP 上报数据，验证后端排行榜服务的处理逻辑和实时排名变动。
          </p>
          <el-button type="primary" plain @click="syncDialog = true" class="shadow-sm w-full">发起模拟上报</el-button>
        </div>
      </div>
    </el-card>

    <!-- 强制改分弹窗 -->
    <el-dialog v-model="forceScoreDialog" title="强制修改榜单分数" width="480px" destroy-on-close class="rounded-xl">
      <el-form :model="forceForm" label-width="90px" class="mt-4">
        <el-form-item label="Redis Key">
          <el-input v-model="forceForm.key" placeholder="如 game:lb:global:1" />
        </el-form-item>
        <el-form-item label="用户 ID">
          <el-input v-model="forceForm.userId" placeholder="输入玩家的自增ID" />
        </el-form-item>
        <el-form-item label="目标分数">
          <el-input-number v-model="forceForm.score" :min="0" :step="1" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="forceScoreDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForceScore" :loading="forceLoading">确认修改</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 手工同步弹窗 -->
    <el-dialog v-model="syncDialog" title="模拟通关上报" width="480px" destroy-on-close class="rounded-xl">
      <el-form :model="syncForm" label-width="90px" class="mt-4">
        <el-form-item label="游戏 ID">
          <el-input-number v-model="syncForm.gameId" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="用户 ID">
          <el-input v-model="syncForm.userId" placeholder="输入玩家的自增ID" />
        </el-form-item>
        <el-form-item label="关卡 ID">
          <el-input v-model="syncForm.levelId" placeholder="如 level_10" />
        </el-form-item>
        <el-form-item label="省份 ID">
          <el-input-number v-model="syncForm.provinceId" :min="1" :controls="false" class="w-full" placeholder="选填，如 440000" />
        </el-form-item>
        <el-form-item label="城市 ID">
          <el-input-number v-model="syncForm.cityId" :min="1" :controls="false" class="w-full" placeholder="选填，如 440300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="syncDialog = false">取消</el-button>
          <el-button type="primary" @click="submitSync" :loading="syncLoading">发送上报</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { disasterRecovery, forceScore, syncRecord } from '@/api'

// 容灾
const recoveryGameId = ref<number | undefined>(undefined)
const recoveryLoading = ref(false)

const handleRecovery = () => {
  if (!recoveryGameId.value) {
    return ElMessage.warning('请输入游戏 ID')
  }
  ElMessageBox.confirm(
    `确定要对游戏 [ID: ${recoveryGameId.value}] 进行全量容灾重建吗？该操作会覆盖 Redis 数据。`,
    '容灾确认',
    { confirmButtonText: '立即重建', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    recoveryLoading.value = true
    try {
      await disasterRecovery(recoveryGameId.value!)
      ElMessage.success('容灾重建任务已成功触发并完成')
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      recoveryLoading.value = false
    }
  }).catch(() => {})
}

// 强制分数
const forceScoreDialog = ref(false)
const forceLoading = ref(false)
const forceForm = reactive({ key: '', userId: '', score: 0 })

const submitForceScore = async () => {
  if (!forceForm.key || !forceForm.userId) return ElMessage.warning('请填写完整')
  forceLoading.value = true
  try {
    await forceScore(forceForm.key, forceForm.userId, forceForm.score)
    ElMessage.success('分数修改成功')
    forceScoreDialog.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    forceLoading.value = false
  }
}

// 模拟上报
const syncDialog = ref(false)
const syncLoading = ref(false)
const syncForm = reactive({
  gameId: 1,
  userId: '',
  levelId: 'level_1',
  provinceId: undefined,
  cityId: undefined,
  districtId: undefined,
  timestamp: Date.now()
})

const submitSync = async () => {
  if (!syncForm.userId || !syncForm.levelId) return ElMessage.warning('请填写必填项')
  syncLoading.value = true
  try {
    syncForm.timestamp = Date.now()
    await syncRecord(syncForm)
    ElMessage.success('模拟上报成功')
    syncDialog.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    syncLoading.value = false
  }
}
</script>
