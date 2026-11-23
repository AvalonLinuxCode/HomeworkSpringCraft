<template>
  <div class="page">
    <div class="page-head">
      <h2>讲师管理</h2>
      <el-button type="primary" @click="openCreate">新增讲师</el-button>
    </div>

    <el-form inline class="search-bar" @submit.prevent>
      <el-form-item label="姓名">
        <el-input v-model="keyword" placeholder="按姓名搜索" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column prop="title" label="职称" width="180" />
      <el-table-column prop="description" label="简介" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next, sizes"
      :total="total"
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑讲师' : '新增讲师'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.avatar" maxlength="255" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { instructorApi } from '@/api/instructor'
import type { InstructorDTO, InstructorVO } from '@/types'

const records = ref<InstructorVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<InstructorDTO>({ name: '', title: '', avatar: '', description: '' })
const rules: FormRules = {
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await instructorApi.page({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    records.value = data.records
    total.value = data.total
  } catch {
    // client.ts 拦截器已经弹过提示，这里只负责别再抛出去
  } finally {
    loading.value = false
  }
}

function reset() {
  keyword.value = ''
  pageNum.value = 1
  load()
}

function onPageChange(page: number) {
  pageNum.value = page
  load()
}

function onSizeChange(size: number) {
  pageSize.value = size
  pageNum.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', title: '', avatar: '', description: '' })
  dialogVisible.value = true
}

function openEdit(row: InstructorVO) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    title: row.title ?? '',
    avatar: row.avatar ?? '',
    description: row.description ?? ''
  })
  dialogVisible.value = true
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (editingId.value) {
        await instructorApi.update(editingId.value, { ...form })
        ElMessage.success('更新成功')
      } else {
        await instructorApi.create({ ...form })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      load()
    } catch {
      // 拦截器已提示
    } finally {
      saving.value = false
    }
  })
}

async function remove(row: InstructorVO) {
  try {
    await ElMessageBox.confirm(`确定删除讲师「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return // 用户点了取消，不是错误
  }
  try {
    await instructorApi.remove(row.id)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    load()
  } catch {
    // 拦截器已提示
  }
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.search-bar {
  margin-bottom: 4px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>