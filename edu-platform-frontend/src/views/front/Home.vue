<template>
  <div>
    <el-carousel height="280px" class="hero" :interval="5000">
      <el-carousel-item v-for="(item, idx) in banners" :key="idx">
        <div class="hero-slide">
          <h1>{{ item.title }}</h1>
          <p>{{ item.subtitle }}</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <section class="section">
      <div class="section-head">
        <h2>热门课程</h2>
        <router-link to="/courses" class="more">查看全部 →</router-link>
      </div>
      <el-row :gutter="16">
        <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8">
          <router-link :to="`/courses/${course.id}`" class="card-link">
            <el-card shadow="hover" class="course-card">
              <div class="cover">{{ course.title.slice(0, 1) }}</div>
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-desc">{{ course.description || '暂无简介' }}</p>
              <div class="card-meta">
                <span>{{ course.instructorName || '未指定讲师' }}</span>
                <span class="price">¥ {{ course.price }}</span>
              </div>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
      <el-empty v-if="!courses.length && !loading" description="暂无已上架课程" />
    </section>

    <section class="section">
      <div class="section-head">
        <h2>名师风采</h2>
        <router-link to="/instructors" class="more">查看全部 →</router-link>
      </div>
      <el-row :gutter="16">
        <el-col v-for="ins in instructors" :key="ins.id" :xs="24" :sm="12" :md="8">
          <router-link :to="`/instructors/${ins.id}`" class="card-link">
            <el-card shadow="hover" class="ins-card">
              <el-avatar :size="56">{{ ins.name.slice(0, 1) }}</el-avatar>
              <h3 class="card-title">{{ ins.name }}</h3>
              <p class="card-desc">{{ ins.title || '讲师' }}</p>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
      <el-empty v-if="!instructors.length && !loading" description="暂无讲师" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { courseApi } from '@/api/course'
import { instructorApi } from '@/api/instructor'
import type { CourseVO, InstructorVO } from '@/types'

const banners = [
  { title: '在线教育平台', subtitle: '精选名师课程，随时随地学习' },
  { title: '课程浏览', subtitle: '按分类、讲师快速找到想要的课程' },
  { title: '后台管理', subtitle: '讲师 / 分类 / 课程一站式维护' }
]

const courses = ref<CourseVO[]>([])
const instructors = ref<InstructorVO[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [coursePage, insList] = await Promise.all([
      courseApi.search({ pageNum: 1, pageSize: 6 }),
      instructorApi.listAll()
    ])
    courses.value = coursePage.records
    instructors.value = insList.slice(0, 6)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 32px;
}
.hero-slide {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(120deg, #409eff, #79bbff);
}
.hero-slide h1 {
  font-size: 32px;
  margin: 0 0 12px;
}
.section {
  margin-bottom: 40px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-head h2 {
  margin: 0;
  font-size: 20px;
}
.more {
  color: #409eff;
  font-size: 14px;
}
.card-link {
  display: block;
  margin-bottom: 16px;
}
.course-card {
  height: 100%;
}
.cover {
  height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  background: linear-gradient(135deg, #909399, #c0c4cc);
  border-radius: 4px;
  margin-bottom: 12px;
}
.card-title {
  margin: 8px 0;
  font-size: 16px;
}
.card-desc {
  color: #909399;
  font-size: 13px;
  min-height: 36px;
  margin: 0 0 8px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
}
.price {
  color: #f56c6c;
  font-weight: 600;
}
.ins-card {
  text-align: center;
  height: 100%;
}
</style>