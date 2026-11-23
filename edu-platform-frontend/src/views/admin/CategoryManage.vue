<template>
  <div class="page">
    <div class="page-head">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openCreate(null)">新增根分类</el-button>
    </div>

    <el-table
      :data="tree"
      v-loading="loading"
      border
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" label="名称" min-width="220" />
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="level" label="层级" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCreate(row.id)">新增子类</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            clearable
            check-strictly
            placeholder="留空则为根分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { categoryApi } from '@/api/category'
import type { CategoryDTO, CategoryVO } from '@/types'

const tree = ref<CategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CategoryDTO>({ name: '', parentId: null, sortOrder: 0 })
const rules: FormRules = {
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
}

const dialogTitle = computed(() => (editingId.value ? '编辑分类' : '新增分类'))

// 编辑时排除自身与自身子树，避免把分类挂到自己的后代下形成环。
const parentOptions = computed<CategoryVO[]>(() => stripSubtree(tree.value, editingId.value))

function stripSubtree(nodes: CategoryVO[], excludeId: number | null): CategoryVO[] {
  return nodes
    .filter((node) => node.id !== excludeId)
    .map((node) => ({ ...node, children: stripSubtree(node.children ?? [], excludeId) }))
}

async function load() {
  loading.value = true
  try {
    tree.value = await categoryApi.tree()
  } catch {
    // client.ts 拦截器已经弹过提示，这里只负责别再抛出去
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number | null) {
  editingId.value = null
  Object.assign(form, { name: '', parentId, sortOrder: 0 })
  dialogVisible.value = true
}

function openEdit(row: CategoryVO) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    parentId: row.parentId,
    sortOrder: row.sortOrder ?? 0
  })
  dialogVisible.value = true
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload: CategoryDTO = {
        name: form.name,
        parentId: form.parentId ?? null,
        sortOrder: form.sortOrder ?? 0
      }
      if (editingId.value) {
        await categoryApi.update(editingId.value, payload)
        ElMessage.success('更新成功')
      } else {
        await categoryApi.create(payload)
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

async function remove(row: CategoryVO) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return // 用户点了取消，不是错误
  }
  try {
    await categoryApi.remove(row.id)
    ElMessage.success('删除成功')
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
</style>