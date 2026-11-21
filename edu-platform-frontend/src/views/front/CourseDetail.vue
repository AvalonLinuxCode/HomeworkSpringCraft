<template>
  <div v-loading="loading">
    <el-breadcrumb separator="/" class="crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/courses' }">课程</el-breadcrumb-item>
      <el-breadcrumb-item>{{ course?.title || '详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card v-if="course">
      <div class="detail-head">
        <div class="cover">{{ course.title.slice(0, 1) }}</div>
        <div class="meta">
          <h1>{{ course.title }}</h1>
          <p class="desc">{{ course.description || '暂无简介' }}</p>
          <div class="tags">
            <el-tag v-for="tag in course.tags" :key="tag" size="small" class="tag">{{ tag }}</el-tag>
          </div>
          <div class="info-grid">
            <div><span class="label">讲师：</span>{{ course.instructorName || '未指定' }}</div>
            <div><span class="label">分类：</span>{{ course.categoryName || '未归类' }}</div>
            <div><span class="label">状态：</span>{{ statusText(course.status) }}</div>
            <div class="price">¥ {{ course.price }}</div>
          </div>
        </div>
      </div>

      <el-divider />
      <h2 class="section-title">课程详情</h2>
      <pre class="details">{{ prettyDetails }}</pre>
    </el-card>

    <el-empty v-else-if="!loading" description="课程不存在或已下架" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { courseApi } from '@/api/course'
import type { CourseDetailVO } from '@/types'

const route = useRoute()
const course = ref<CourseDetailVO | null>(null)
const loading = ref(false)

const prettyDetails = computed(() => {
  if (!course.value?.details) return '暂无详细内容'
  try {
    return JSON.stringify(JSON.parse(course.value.details), null, 2)
  } catch {
    return course.value.details
  }
})

function statusText(status: string) {
  return status === 'published' ? '已上架' : status === 'archived' ? '已归档' : '草稿'
}

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    course.value = await courseApi.frontGetById(id)
  } catch {
    course.value = null
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
.detail-head {
  display: flex;
  gap: 24px;
}
.cover {
  width: 220px;
  height: 150px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  color: #fff;
  background: linear-gradient(135deg, #909399, #c0c4cc);
  border-radius: 6px;
}
.meta h1 {
  margin: 0 0 8px;
  font-size: 24px;
}
.desc {
  color: #606266;
  margin: 0 0 12px;
}
.tags {
  margin-bottom: 12px;
}
.tag {
  margin-right: 6px;
}
.info-grid {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  color: #606266;
  font-size: 14px;
}
.label {
  color: #909399;
}
.price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 18px;
}
.section-title {
  font-size: 16px;
  margin: 0 0 12px;
}
.details {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  color: #606266;
  margin: 0;
}
</style>