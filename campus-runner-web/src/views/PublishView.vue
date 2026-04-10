<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        
        <el-form-item label="任务分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="取件地址" prop="pickupAddress">
          <el-input v-model="form.pickupAddress" placeholder="请输入取件地址" />
        </el-form-item>
        
        <el-form-item label="送达地址" prop="deliveryAddress">
          <el-input v-model="form.deliveryAddress" placeholder="请输入送达地址" />
        </el-form-item>
        
        <el-form-item label="任务酬金" prop="reward">
          <el-input-number v-model="form.reward" :min="1" :precision="2" />
          <span class="unit">元</span>
        </el-form-item>
        
        <el-form-item label="物品重量">
          <el-input-number v-model="form.weight" :min="0" :precision="2" />
          <span class="unit">kg</span>
        </el-form-item>
        
        <el-form-item label="期望完成时间">
          <el-date-picker
            v-model="form.expectCompleteTime"
            type="datetime"
            placeholder="选择时间"
          />
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="联系人姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="联系人电话" />
        </el-form-item>
        
        <el-form-item label="任务描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述任务要求"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handlePublish">
            发布任务
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { publishTask } from '@/api/task'
import { getCategoryList } from '@/api/task'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const categories = ref<any[]>([])

const form = ref({
  title: '',
  categoryId: '',
  pickupAddress: '',
  deliveryAddress: '',
  reward: 5,
  weight: 0,
  expectCompleteTime: '',
  contactName: '',
  contactPhone: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择任务分类', trigger: 'change' }],
  pickupAddress: [{ required: true, message: '请输入取件地址', trigger: 'blur' }],
  deliveryAddress: [{ required: true, message: '请输入送达地址', trigger: 'blur' }],
  reward: [{ required: true, message: '请输入酬金', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

onMounted(() => {
  loadCategories()
})

const loadCategories = async () => {
  try {
    const res: any = await getCategoryList()
    categories.value = res.result
  } catch (error) {
    console.error(error)
  }
}

const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  // 将 ISO 8601 格式 (2026-03-12T16:00:00.000Z) 转换为 yyyy-MM-dd HH:mm:ss
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handlePublish = async () => {
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const submitData = {
          ...form.value,
          expectCompleteTime: formatDateTime(form.value.expectCompleteTime)
        }
        await publishTask(submitData)
        ElMessage.success('发布成功')
        router.push('/tasks')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.publish-container {
  max-width: 800px;
  margin: 0 auto;
}

.publish-card {
  padding: 20px;
}

.unit {
  margin-left: 8px;
  color: #666;
}
</style>
