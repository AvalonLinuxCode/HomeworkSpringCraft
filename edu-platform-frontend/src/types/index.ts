// 与后端 Result<T> / PageResult<T> / VO 一一对应的类型定义。

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface InstructorVO {
  id: number
  name: string
  avatar: string | null
  title: string | null
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface InstructorDTO {
  name: string
  avatar?: string | null
  title?: string | null
  description?: string | null
}

export interface CourseVO {
  id: number
  title: string
  description: string | null
  price: number
  instructorId: number
  instructorName: string | null
  categoryId: number | null
  categoryName: string | null
  status: 'draft' | 'published' | 'archived'
  coverImage: string | null
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface CourseDetailVO extends CourseVO {
  details: string | null
}

export interface CourseDTO {
  title: string
  description?: string | null
  price: number
  instructorId: number
  categoryId?: number | null
  coverImage?: string | null
  tags?: string[]
  details?: string | null
}

export interface CourseQuery {
  keyword?: string
  pageNum: number
  pageSize: number
  instructorId?: number
  categoryId?: number
  status?: string
}

export interface CategoryVO {
  id: number
  name: string
  parentId: number | null
  sortOrder: number | null
  level: number | null
  children: CategoryVO[]
}

export interface CategoryDTO {
  name: string
  parentId?: number | null
  sortOrder?: number | null
}