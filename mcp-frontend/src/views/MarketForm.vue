<template>
  <div class="market-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑市场' : '添加市场' }}</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="市场名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入市场名称" />
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input v-model="form.url" placeholder="请输入市场 URL" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入市场描述" />
        </el-form-item>
        <el-form-item label="认证配置" prop="authConfig">
          <el-input v-model="form.authConfig" type="textarea" :rows="5" placeholder="请输入 JSON 格式的认证配置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ENABLED">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
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
import { getMarketById, createMarket, updateMarket, type McpMarket } from '@/api/market'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const isEdit = ref(false)
const form = reactive<McpMarket>({
  name: '',
  url: '',
  description: '',
  authConfig: '',
  status: 'ENABLED'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入市场名称', trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadData = async () => {
  const id = route.params.id as string
  if (id) {
    isEdit.value = true
    try {
      const res = await getMarketById(Number(id))
      if (res.success && res.data) {
        Object.assign(form, res.data)
      }
    } catch (error) {
      ElMessage.error('加载市场信息失败')
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
          res = await updateMarket(form.id!, form)
        } else {
          res = await createMarket(form)
        }
        if (res.success) {
          ElMessage.success('保存成功')
          router.push('/markets')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const handleCancel = () => {
  router.push('/markets')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.market-form {
  height: 100%;
  margin: 20px;
}

:deep(.el-card) {
  height: 100%;
}
</style>

