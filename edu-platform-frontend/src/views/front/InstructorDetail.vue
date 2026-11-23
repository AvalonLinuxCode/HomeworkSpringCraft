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

    <section v-if="instructor" class="section">
      <h2 class="section-title">所授课程</h2>
      <el-row :gutter="16">
        <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8">
          <router-link :to="`/courses/${course.id}`" class="card-link">
            <el-card shadow="hover" class="course-card">
              <div class="cover">{{ course.title.slice(0, 1) }}</div>
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-desc">{{ course.description || '暂无简介' }}</p>
              <div class="card-meta">
                <span>{{ course.categoryName || '未分类' }}</span>
                <span class="price">¥ {{ course.price }}</span>
              </div>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
      <el-empty v-if="!courses.length && !loading" description="暂无已上架课程" />
    </section>

    <el-empty v-else-if="!loading" description="讲师不存在" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { instructorApi } from '@/api/instructor'
import type { CourseVO, InstructorVO } from '@/types'

const route = useRoute()
const instructor = ref<InstructorVO | null>(null)
const courses = ref<CourseVO[]>([])
const loading = ref(false)

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  instructor.value = null
  courses.value = []
  try {
    instructor.value = await instructorApi.frontGetById(id)
  } catch {
    // 拦截器已提示；讲师取不到才按「不存在」显示
    loading.value = false
    return
  }
  try {
    courses.value = await instructorApi.frontCourses(id)
  } catch {
    // 课程取不到只让「所授课程」显示空态，不能把讲师也一并判成不存在
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
.section {
  margin-top: 24px;
}
.section-title {
  font-size: 18px;
  margin: 0 0 16px;
}
.card-link {
  display: block;
  margin-bottom: 16px;
}
.card-title {
  font-size: 15px;
  margin: 0 0 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-desc {
  color: #909399;
  font-size: 13px;
  margin: 0 0 10px;
  min-height: 34px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
}
.price {
  color: #f56c6c;
  font-weight: 600;
}
</style>
