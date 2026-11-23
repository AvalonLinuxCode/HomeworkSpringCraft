<template>
  <div v-loading="loading">
    <h2 class="title">讲师列表</h2>
    <el-row :gutter="16">
      <el-col v-for="ins in instructors" :key="ins.id" :xs="24" :sm="12" :md="8">
        <router-link :to="`/instructors/${ins.id}`" class="card-link">
          <el-card shadow="hover" class="ins-card">
            <el-avatar :size="64">{{ ins.name.slice(0, 1) }}</el-avatar>
            <h3>{{ ins.name }}</h3>
            <p class="title-text">{{ ins.title || '讲师' }}</p>
            <p class="desc">{{ ins.description || '暂无简介' }}</p>
          </el-card>
        </router-link>
      </el-col>
    </el-row>
    <el-empty v-if="!instructors.length && !loading" description="暂无讲师" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { instructorApi } from '@/api/instructor'
import type { InstructorVO } from '@/types'

const instructors = ref<InstructorVO[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    instructors.value = await instructorApi.listAll()
  } catch {
    // client.ts 拦截器已经弹过提示，这里只负责别再抛出去
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.title {
  font-size: 20px;
  margin: 0 0 20px;
}
.card-link {
  display: block;
  margin-bottom: 16px;
}
.ins-card {
  text-align: center;
  height: 100%;
}
.ins-card h3 {
  margin: 10px 0 4px;
}
.title-text {
  color: #409eff;
  font-size: 13px;
  margin: 0 0 8px;
}
.desc {
  color: #909399;
  font-size: 13px;
  min-height: 36px;
  margin: 0;
}
</style>