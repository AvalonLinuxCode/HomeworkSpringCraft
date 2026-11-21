<template>
  <div>
    <div class="head">
      <h2>课程列表</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索课程标题/简介"
        clearable
        style="max-width: 320px"
        @keyup.enter="search"
      >
        <template #append>
          <el-button @click="search">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="course in records" :key="course.id" :xs="24" :sm="12" :md="8">
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

    <el-empty v-if="!records.length && !loading" description="没有找到课程" />

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :current-page="pageNum"
      :page-size="pageSize"
      @current-change="onPageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { courseApi } from '@/api/course'
import type { CourseVO } from '@/types'

const records = ref<CourseVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(9)
const keyword = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await courseApi.search({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  load()
}

function onPageChange(page: number) {
  pageNum.value = page
  load()
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.head h2 {
  margin: 0;
  font-size: 20px;
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
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>