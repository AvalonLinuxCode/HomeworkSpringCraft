<template>
  <div v-loading="loading">
    <el-breadcrumb separator="/" class="crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/instructors' }">讲师</el-breadcrumb-item>
      <el-breadcrumb-item>{{ instructor?.name || '详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card v-if="instructor">
      <div class="head">
        <el-avatar :size="88">{{ instructor.name.slice(0, 1) }}</el-avatar>
        <div class="meta">
          <h1>{{ instructor.name }}</h1>
          <p class="title-text">{{ instructor.title || '讲师' }}</p>
          <p class="desc">{{ instructor.description || '暂无简介' }}</p>
        </div>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="讲师不存在" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { instructorApi } from '@/api/instructor'
import type { InstructorVO } from '@/types'

const route = useRoute()
const instructor = ref<InstructorVO | null>(null)
const loading = ref(false)

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    instructor.value = await instructorApi.frontGetById(id)
  } catch {
    instructor.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.crumb {
  margin-bottom: 16px;
}
.head {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.meta h1 {
  margin: 0 0 6px;
  font-size: 24px;
}
.title-text {
  color: #409eff;
  margin: 0 0 12px;
}
.desc {
  color: #606266;
  margin: 0;
}
</style>