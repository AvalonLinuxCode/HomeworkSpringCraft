import http from './client'
import type { InstructorDTO, InstructorVO, PageResult } from '@/types'

export const instructorApi = {
  // 后台分页列表
  page(params: { keyword?: string; pageNum: number; pageSize: number }) {
    return http.get<unknown, PageResult<InstructorVO>>('/admin/instructors', { params })
  },

  // 后台详情
  getById(id: number) {
    return http.get<unknown, InstructorVO>(`/admin/instructors/${id}`)
  },

  create(dto: InstructorDTO) {
    return http.post<unknown, number>('/admin/instructors', dto)
  },

  update(id: number, dto: InstructorDTO) {
    return http.put<unknown, void>(`/admin/instructors/${id}`, dto)
  },

  remove(id: number) {
    return http.delete<unknown, void>(`/admin/instructors/${id}`)
  },

  // 前台全量列表
  listAll() {
    return http.get<unknown, InstructorVO[]>('/front/instructors')
  },

  // 前台详情
  frontGetById(id: number) {
    return http.get<unknown, InstructorVO>(`/front/instructors/${id}`)
  }
}