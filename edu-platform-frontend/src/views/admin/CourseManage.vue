<template>
  <div class="page">
    <div class="page-head">
      <h2>课程管理</h2>
      <el-button type="primary" @click="openCreate">新增课程</el-button>
    </div>

    <el-form inline class="search-bar" @submit.prevent>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="标题/描述" clearable @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="讲师">
        <el-select v-model="query.instructorId" clearable placeholder="全部" style="width: 160px">
          <el-option v-for="i in instructors" :key="i.id" :label="i.name" :value="i.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" clearable placeholder="全部" style="width: 160px">
          <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="草稿" value="draft" />
          <el-option label="已上架" value="published" />
          <el-option label="已归档" value="archived" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥ {{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="instructorName" label="讲师" width="120" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
          <el-button v-if="row.status !== 'published'" link type="success" @click="publish(row)">上架</el-button>
          <el-button v-else link type="warning" @click="unpublish(row)">下架</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next, sizes"
      :total="total"
      :current-page="query.pageNum"
      :page-size="query.pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="讲师" prop="instructorId">
          <el-select v-model="form.instructorId" placeholder="选择讲师" style="width: 100%">
            <el-option v-for="i in instructors" :key="i.id" :label="i.name" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="封面 URL">
          <el-input v-model="form.coverImage" maxlength="255" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple filterable allow-create default-first-option placeholder="回车新增" style="width: 100%">
            <el-option v-for="t in form.tags ?? []" :key="t" :label="t" :value="t" />
          </el-select>
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
import { courseApi } from '@/api/course'
import { instructorApi } from '@/api/instructor'
import { categoryApi } from '@/api/category'
import type { CategoryVO, CourseDTO, CourseVO, InstructorVO } from '@/types'

const records = ref<CourseVO[]>([])
const total = ref(0)
const loading = ref(false)
const instructors = ref<InstructorVO[]>([])
const flatCategories = ref<CategoryVO[]>([])

const query = reactive({
  keyword: '',
  instructorId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CourseDTO & { tags: string[] }>({
  title: '',
  description: '',
  price: 0,
  instructorId: 0,
  categoryId: null,
  coverImage: '',
  tags: [],
  details: ''
})
const rules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  instructorId: [{ required: true, message: '请选择讲师', trigger: 'change' }]
}

function flatten(nodes: CategoryVO[]): CategoryVO[] {
  const out: CategoryVO[] = []
  for (const node of nodes) {
    out.push(node)
    if (node.children?.length) out.push(...flatten(node.children))
  }
  return out
}

async function loadReference() {
  const [ins, cats] = await Promise.all([instructorApi.listAll(), categoryApi.tree()])
  instructors.value = ins
  flatCategories.value = flatten(cats)
}

async function load() {
  loading.value = true
  try {
    const data = await courseApi.page({ ...query })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function reset() {
  Object.assign(query, {
    keyword: '',
    instructorId: undefined,
    categoryId: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10
  })
  load()
}

function onPageChange(page: number) {
  query.pageNum = page
  load()
}

function onSizeChange(size: number) {
  query.pageSize = size
  query.pageNum = 1
  load()
}

function statusText(status: string) {
  return status === 'published' ? '已上架' : status === 'archived' ? '已归档' : '草稿'
}

function statusTag(status: string): 'success' | 'info' | 'warning' {
  return status === 'published' ? 'success' : status === 'archived' ? 'info' : 'warning'
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    description: '',
    price: 0,
    instructorId: instructors.value[0]?.id ?? 0,
    categoryId: null,
    coverImage: '',
    tags: [],
    details: ''
  })
  dialogVisible.value = true
}

async function openEdit(id: number) {
  const detail = await courseApi.getById(id)
  editingId.value = id
  Object.assign(form, {
    title: detail.title,
    description: detail.description ?? '',
    price: detail.price,
    instructorId: detail.instructorId,
    categoryId: detail.categoryId,
    coverImage: detail.coverImage ?? '',
    tags: detail.tags ?? [],
    details: detail.details ?? ''
  })
  dialogVisible.value = true
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload: CourseDTO = {
        title: form.title,
        description: form.description || null,
        price: form.price,
        instructorId: form.instructorId,
        categoryId: form.categoryId ?? null,
        coverImage: form.coverImage || null,
        tags: form.tags ?? [],
        details: form.details || null
      }
      if (editingId.value) {
        await courseApi.update(editingId.value, payload)
        ElMessage.success('更新成功')
      } else {
        await courseApi.create(payload)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      load()
    } finally {
      saving.value = false
    }
  })
}

async function publish(row: CourseVO) {
  await courseApi.publish(row.id)
  ElMessage.success('已上架')
  load()
}

async function unpublish(row: CourseVO) {
  await courseApi.unpublish(row.id)
  ElMessage.success('已下架')
  load()
}

async function remove(row: CourseVO) {
  await ElMessageBox.confirm(`确定删除课程「${row.title}」吗？`, '提示', { type: 'warning' })
  await courseApi.remove(row.id)
  ElMessage.success('删除成功')
  if (records.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  load()
}

onMounted(async () => {
  await loadReference()
  load()
})
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