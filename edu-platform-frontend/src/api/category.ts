import http from './client'
import type { CategoryDTO, CategoryVO } from '@/types'

export const categoryApi = {
  tree() {
    return http.get<unknown, CategoryVO[]>('/admin/categories/tree')
  },

  create(dto: CategoryDTO) {
    return http.post<unknown, number>('/admin/categories', dto)
  },

  update(id: number, dto: CategoryDTO) {
    return http.put<unknown, void>(`/admin/categories/${id}`, dto)
  },

  remove(id: number) {
    return http.delete<unknown, void>(`/admin/categories/${id}`)
  }
}