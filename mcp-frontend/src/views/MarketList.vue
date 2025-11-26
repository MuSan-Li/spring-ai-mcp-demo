<template>
  <div class="market-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>MCP 市场管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加市场
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入市场名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="marketList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="市场名称" />
        <el-table-column prop="url" label="URL" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMarketList, deleteMarket, updateMarketStatus, type McpMarket } from '@/api/market'

const router = useRouter()

const loading = ref(false)
const marketList = ref<McpMarket[]>([])

const searchForm = ref({
  keyword: '',
  status: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMarketList(searchForm.value)
    if (res.success && res.data) {
      marketList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载市场列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    status: ''
  }
  loadData()
}

const handleAdd = () => {
  router.push('/markets/add')
}

const handleEdit = (row: McpMarket) => {
  router.push(`/markets/edit/${row.id}`)
}

const handleDetail = (row: McpMarket) => {
  router.push(`/markets/${row.id}`)
}

const handleToggleStatus = async (row: McpMarket) => {
  const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  try {
    const res = await updateMarketStatus(row.id!, newStatus)
    if (res.success) {
      ElMessage.success('状态更新成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row: McpMarket) => {
  try {
    await ElMessageBox.confirm('确定要删除该市场吗？', '提示', {
      type: 'warning'
    })
    const res = await deleteMarket(row.id!)
    if (res.success) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.market-list {
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
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>

