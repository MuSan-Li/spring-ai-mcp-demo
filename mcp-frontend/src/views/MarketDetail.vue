<template>
  <div class="market-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>市场详情：{{ market?.name }}</span>
          <div>
            <el-button type="primary" @click="handleRefresh">刷新工具列表</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="市场名称">{{ market?.name }}</el-descriptions-item>
        <el-descriptions-item label="URL">{{ market?.url }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ market?.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="market?.status === 'ENABLED' ? 'success' : 'danger'">
            {{ market?.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ market?.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>工具列表</el-divider>

      <div class="toolbar">
        <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleBatchLoad">
          批量加载到本地
        </el-button>
      </div>

      <el-table :data="toolList" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="toolName" label="工具名称" />
        <el-table-column prop="toolDescription" label="描述" show-overflow-tooltip />
        <el-table-column prop="isLoaded" label="已加载" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isLoaded ? 'success' : 'info'">
              {{ row.isLoaded ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isLoaded"
              link
              type="primary"
              @click="handleLoad(row)"
            >
              加载到本地
            </el-button>
            <span v-else>已加载</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMarketById, getMarketTools, refreshMarketTools, loadMarketTool, batchLoadMarketTools, type McpMarket, type McpMarketTool } from '@/api/market'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const market = ref<McpMarket>()
const toolList = ref<McpMarketTool[]>([])
const selectedIds = ref<number[]>([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const loadMarket = async () => {
  const id = Number(route.params.id)
  try {
    const res = await getMarketById(id)
    if (res.success && res.data) {
      market.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载市场信息失败')
  }
}

const loadTools = async () => {
  loading.value = true
  const id = Number(route.params.id)
  try {
    const res = await getMarketTools(id, pagination.value.page, pagination.value.size)
    if (res.success && res.data) {
      toolList.value = res.data
      if (res.total !== undefined) {
        pagination.value.total = res.total
      }
    }
  } catch (error) {
    ElMessage.error('加载工具列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  loadTools()
}

const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.page = 1 // 重置到第一页
  loadTools()
}

const handleRefresh = async () => {
  const id = Number(route.params.id)
  try {
    const res = await refreshMarketTools(id)
    if (res.success) {
      ElMessage.success('刷新成功')
      loadTools()
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const handleLoad = async (row: McpMarketTool) => {
  try {
    const res = await loadMarketTool(row.id!)
    if (res.success) {
      ElMessage.success('加载成功')
      loadTools()
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const handleBatchLoad = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要加载的工具')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要加载选中的 ${selectedIds.value.length} 个工具吗？`, '提示', {
      type: 'warning'
    })
    const res = await batchLoadMarketTools(selectedIds.value)
    if (res.success) {
      ElMessage.success(res.message || '批量加载成功')
      selectedIds.value = []
      loadTools()
    } else {
      ElMessage.error(res.message || '批量加载失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量加载失败')
    }
  }
}

const handleSelectionChange = (selection: McpMarketTool[]) => {
  selectedIds.value = selection.map(item => item.id!)
}

const handleBack = () => {
  router.push('/markets')
}

onMounted(() => {
  loadMarket()
  loadTools()
})
</script>

<style scoped>
.market-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  margin: 20px;
}

:deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.el-table__body-wrapper) {
  max-height: calc(100vh - 500px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar {
  margin: 20px 0;
}
</style>

