<template>
  <div class="tool-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑工具' : '添加工具' }}</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="工具名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工具名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入工具描述" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="LOCAL">本地</el-radio>
            <el-radio label="REMOTE">远程</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ENABLED">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="配置信息" prop="configJson">
          <el-input v-model="form.configJson" type="textarea" :rows="10" placeholder="请输入 JSON 配置" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getToolById, createTool, updateTool, type McpToolData } from '@/api/tool'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const isEdit = ref(false)
const form = reactive<McpToolData>({
  name: '',
  description: '',
  type: 'LOCAL',
  status: 'ENABLED',
  configJson: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadData = async () => {
  const id = route.params.id as string
  if (id) {
    isEdit.value = true
    try {
      const res = await getToolById(Number(id))
      if (res.success && res.data) {
        Object.assign(form, res.data)
      }
    } catch (error) {
      ElMessage.error('加载工具信息失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (isEdit.value) {
          res = await updateTool(form.id!, form)
        } else {
          res = await createTool(form)
        }
        if (res.success) {
          ElMessage.success('保存成功')
          router.push('/tools')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const handleCancel = () => {
  router.push('/tools')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tool-form {
  height: 100%;
  margin: 20px;
}

:deep(.el-card) {
  height: 100%;
}
</style>

