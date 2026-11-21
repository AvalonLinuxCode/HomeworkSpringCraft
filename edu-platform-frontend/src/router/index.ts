import { createRouter, createWebHistory } from 'vue-router'

// 路由：后台 /admin/* 与前台 / 分区，共用 AdminLayout / FrontLayout。
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/FrontLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/front/Home.vue') },
        { path: 'courses', name: 'front-courses', component: () => import('@/views/front/CourseList.vue') },
        { path: 'courses/:id', name: 'front-course-detail', component: () => import('@/views/front/CourseDetail.vue') },
        { path: 'instructors', name: 'front-instructors', component: () => import('@/views/front/InstructorList.vue') },
        { path: 'instructors/:id', name: 'front-instructor-detail', component: () => import('@/views/front/InstructorDetail.vue') }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/admin/instructors' },
        { path: 'instructors', name: 'admin-instructors', component: () => import('@/views/admin/InstructorManage.vue') },
        { path: 'courses', name: 'admin-courses', component: () => import('@/views/admin/CourseManage.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManage.vue') }
      ]
    }
  ]
})

export default router