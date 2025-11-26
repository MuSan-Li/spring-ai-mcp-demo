<template>
  <div class="tool-list">
    <el-card class="full-height-card">
      <template #header>
        <div class="card-header">
          <span>MCP 工具管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加工具
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入工具名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="本地" value="LOCAL" />
            <el-option label="远程" value="REMOTE" />
          </el-select>
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

      <el-table :data="toolList" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="工具名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'LOCAL' ? 'success' : 'info'">
              {{ row.type === 'LOCAL' ? '本地' : '远程' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleTest(row)" :disabled="row.status !== 'ENABLED'">
              测试
            </el-button>
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

      <div class="toolbar">
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 测试工具对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      :title="`测试工具: ${currentTestTool?.name}`"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="toolInfo" class="tool-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="工具名称">{{ toolInfo.tool?.name }}</el-descriptions-item>
          <el-descriptions-item label="工具类型">
            <el-tag :type="toolInfo.tool?.type === 'LOCAL' ? 'success' : 'info'">
              {{ toolInfo.tool?.type === 'LOCAL' ? '本地' : '远程' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ toolInfo.tool?.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="注册状态">
            <el-tag :type="toolInfo.tool?.registered ? 'success' : 'warning'">
              {{ toolInfo.tool?.registered ? '已注册' : '未注册' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="toolInfo.tool?.parameters && Array.isArray(toolInfo.tool.parameters) && toolInfo.tool.parameters.length > 0" class="parameters-info">
          <h4>参数说明：</h4>
          <el-table :data="toolInfo.tool.parameters" border size="small" style="margin-top: 10px">
            <el-table-column prop="name" label="参数名" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="required" label="必填" width="80">
              <template #default="{ row }">
                <el-tag :type="row.required ? 'danger' : 'info'">
                  {{ row.required ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 动态参数表单 -->
      <el-form :model="testForm" label-width="120px" style="margin-top: 20px" v-if="toolInfo?.tool?.parameters && Array.isArray(toolInfo.tool.parameters) && toolInfo.tool.parameters.length > 0">
        <el-form-item
          v-for="param in toolInfo.tool.parameters"
          :key="param.name"
          :label="param.name"
          :required="param.required"
        >
          <!-- 枚举类型 -->
          <el-select
            v-if="param.enum && param.enum.length > 0"
            v-model="testForm.params[param.name]"
            :placeholder="param.description || `请选择${param.name}`"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="option in param.enum"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>
          <!-- 数字类型 -->
          <el-input-number
            v-else-if="param.type === 'number' || param.type === 'integer'"
            v-model="testForm.params[param.name]"
            :placeholder="param.description || `请输入${param.name}`"
            style="width: 100%"
          />
          <!-- 布尔类型 -->
          <el-switch
            v-else-if="param.type === 'boolean'"
            v-model="testForm.params[param.name]"
          />
          <!-- 字符串类型（默认） -->
          <el-input
            v-else
            v-model="testForm.params[param.name]"
            :placeholder="param.description || `请输入${param.name}`"
            clearable
          />
          <div v-if="param.description" class="param-hint">{{ param.description }}</div>
        </el-form-item>
      </el-form>

      <!-- 如果没有参数信息，使用 JSON 输入 -->
      <el-form :model="testForm" label-width="100px" style="margin-top: 20px" v-else>
        <el-form-item label="测试参数 (JSON)">
          <el-input
            v-model="testForm.paramsJson"
            type="textarea"
            :rows="6"
            placeholder='请输入 JSON 格式的参数，例如: {"a": 1, "b": "test"}'
          />
          <div class="param-hint">提示：如果工具未注册或无法获取参数信息，请使用 JSON 格式输入参数</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="testDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRunTest" :loading="testing">
          执行测试
        </el-button>
      </template>

      <!-- 测试结果 -->
      <el-divider v-if="testResult">测试结果</el-divider>
      <div v-if="testResult" class="test-result">
        <el-alert
          :type="testResult.success ? 'success' : 'error'"
          :title="testResult.success ? '测试成功' : '测试失败'"
          :closable="false"
          style="margin-bottom: 10px"
        />
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="工具名称">{{ testResult.toolName }}</el-descriptions-item>
          <el-descriptions-item label="执行时间">{{ testResult.duration || '-' }}</el-descriptions-item>
          <el-descriptions-item label="请求参数">
            <pre>{{ JSON.stringify(testResult.request || {}, null, 2) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item v-if="testResult.success" label="响应结果">
            <pre>{{ typeof testResult.response === 'string' ? testResult.response : JSON.stringify(testResult.response, null, 2) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item v-if="!testResult.success" label="错误信息">
            <pre style="color: red">{{ testResult.error || testResult.message }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getToolList, deleteTool, deleteToolsBatch, updateToolStatus, testTool, getToolInfo, type McpToolData } from '@/api/tool'

const router = useRouter()

const loading = ref(false)
const toolList = ref<McpToolData[]>([])
const selectedIds = ref<number[]>([])
const testDialogVisible = ref(false)
const currentTestTool = ref<McpToolData | null>(null)
const toolInfo = ref<any>(null)
const testing = ref(false)
const testResult = ref<any>(null)

const testForm = ref<{
  params: Record<string, any>
  paramsJson: string
}>({
  params: {},
  paramsJson: '{}'
})

const searchForm = ref({
  keyword: '',
  type: '',
  status: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getToolList(searchForm.value)
    if (res.success && res.data) {
      toolList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载工具列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    type: '',
    status: ''
  }
  loadData()
}

const handleAdd = () => {
  router.push('/tools/add')
}

const handleEdit = (row: McpToolData) => {
  router.push(`/tools/edit/${row.id}`)
}

const handleToggleStatus = async (row: McpToolData) => {
  const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  try {
    const res = await updateToolStatus(row.id!, newStatus)
    if (res.success) {
      ElMessage.success('状态更新成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row: McpToolData) => {
  try {
    await ElMessageBox.confirm('确定要删除该工具吗？', '提示', {
      type: 'warning'
    })
    const res = await deleteTool(row.id!)
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

const handleSelectionChange = (selection: McpToolData[]) => {
  selectedIds.value = selection.map(item => item.id!)
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的工具')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个工具吗？`, '提示', {
      type: 'warning'
    })
    const res = await deleteToolsBatch(selectedIds.value.join(','))
    if (res.success) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 从 configJson 中解析参数信息
const parseToolParameters = (configJson: string | undefined) => {
  if (!configJson) return []
  
  try {
    const config = JSON.parse(configJson)
    const functionDef = config.function
    if (!functionDef || !functionDef.parameters) return []
    
    const parameters = functionDef.parameters
    const properties = parameters.properties || {}
    const required = parameters.required || []
    
    const paramList: Array<{
      name: string
      type: string
      description: string
      required: boolean
      enum?: string[]
    }> = []
    
    for (const [name, prop] of Object.entries(properties)) {
      const paramDef = prop as any
      paramList.push({
        name,
        type: paramDef.type || 'string',
        description: paramDef.description || '',
        required: required.includes(name),
        enum: paramDef.enum
      })
    }
    
    return paramList
  } catch (error) {
    console.error('解析 configJson 失败:', error)
    return []
  }
}

const handleTest = async (row: McpToolData) => {
  currentTestTool.value = row
  testForm.value = {
    params: {},
    paramsJson: '{}'
  }
  testResult.value = null
  testDialogVisible.value = true

  // 从 configJson 解析参数信息
  const parameters = parseToolParameters(row.configJson)
  console.log('解析的参数:', parameters)
  
  // 构建工具信息对象
  toolInfo.value = {
    tool: {
      id: row.id,
      name: row.name,
      description: row.description,
      type: row.type,
      status: row.status,
      registered: true, // 假设已注册
      parameters: parameters
    }
  }
  
  // 如果有参数信息，初始化参数表单
  if (parameters && parameters.length > 0) {
    const params: Record<string, any> = {}
    parameters.forEach((param) => {
      // 根据类型设置默认值
      if (param.type === 'boolean') {
        params[param.name] = false
      } else if (param.type === 'number' || param.type === 'integer') {
        params[param.name] = undefined // 数字类型不设置默认值，让用户输入
      } else if (param.enum && param.enum.length > 0) {
        // 枚举类型，设置第一个选项为默认值
        params[param.name] = param.enum[0]
      } else {
        params[param.name] = ''
      }
    })
    testForm.value.params = params
    console.log('初始化参数表单:', params)
  }
}

const handleRunTest = async () => {
  if (!currentTestTool.value) return

  // 根据是否有参数信息决定使用哪种方式
  let params: Record<string, any> = {}
  
  if (toolInfo.value?.tool?.parameters && toolInfo.value.tool.parameters.length > 0) {
    // 使用动态表单参数
    // 过滤掉空值（可选参数），但保留必填参数
    const paramDefs = toolInfo.value.tool.parameters as Array<{ name: string; required: boolean }>
    for (const paramDef of paramDefs) {
      const value = testForm.value.params[paramDef.name]
      // 如果是必填参数且值为空，提示错误
      if (paramDef.required && (value === undefined || value === null || value === '')) {
        ElMessage.warning(`参数 ${paramDef.name} 是必填项，请输入值`)
        return
      }
      // 如果值不为空，添加到参数对象
      if (value !== undefined && value !== null && value !== '') {
        params[paramDef.name] = value
      }
    }
  } else {
    // 使用 JSON 输入
    try {
      if (testForm.value.paramsJson.trim()) {
        params = JSON.parse(testForm.value.paramsJson)
      }
    } catch (error) {
      ElMessage.error('参数格式错误，请输入有效的 JSON 格式')
      return
    }
  }

  testing.value = true
  testResult.value = null

  try {
    const res = await testTool(currentTestTool.value.id!, params)
    // 后端直接返回 Map，axios 拦截器会返回 response.data
    // 如果 res 是 ApiResponse 格式，则取 res.data；否则直接使用 res
    const result = (res as any).data || res
    testResult.value = result
    if (result.success) {
      ElMessage.success('测试执行成功')
    } else {
      ElMessage.error(result.error || result.message || '测试执行失败')
    }
  } catch (error: any) {
    testResult.value = {
      success: false,
      error: error?.response?.data?.error || error?.message || '测试执行失败'
    }
    ElMessage.error('测试执行失败')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tool-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-top: 20px;
}

.full-height-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  margin: 20px;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.el-table) {
  flex: 1;
}

:deep(.el-table__body-wrapper) {
  max-height: calc(100vh - 400px);
  overflow-y: auto;
}

.tool-info {
  margin-bottom: 20px;
}

.parameters-info {
  margin-top: 15px;
}

.parameters-info h4 {
  margin: 10px 0;
  font-size: 14px;
  font-weight: 600;
}

.test-result {
  margin-top: 20px;
}

.test-result pre {
  margin: 0;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow-x: auto;
  max-height: 300px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.5;
}

.param-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

