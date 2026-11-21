import http from './client'
import type { CourseDTO, CourseDetailVO, CourseQuery, CourseVO, PageResult } from '@/types'

export const courseApi = {
  // 后台分页列表（可按关键字/讲师/分类/状态过滤）
  page(query: CourseQuery) {
    return http.get<unknown, PageResult<CourseVO>>('/admin/courses', { params: query })
  },

  getById(id: number) {
    return http.get<unknown, CourseDetailVO>(`/admin/courses/${id}`)
  },

  create(dto: CourseDTO) {
    return http.post<unknown, number>('/admin/courses', dto)
  },

  update(id: number, dto: CourseDTO) {
    return http.put<unknown, void>(`/admin/courses/${id}`, dto)
  },

  remove(id: number) {
    return http.delete<unknown, void>(`/admin/courses/${id}`)
  },

  publish(id: number) {
    return http.post<unknown, void>(`/admin/courses/${id}/publish`)
  },

  unpublish(id: number) {
    return http.post<unknown, void>(`/admin/courses/${id}/unpublish`)
  },

  // 前台搜索：服务端只返回 published
  search(params: { keyword?: string; pageNum: number; pageSize: number }) {
    return http.get<unknown, PageResult<CourseVO>>('/front/courses', { params })
  },

  frontGetById(id: number) {
    return http.get<unknown, CourseDetailVO>(`/front/courses/${id}`)
  }
}