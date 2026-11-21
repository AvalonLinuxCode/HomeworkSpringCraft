# 体系结构设计文档

> 基于 `Document_Analysis.md` 第三章体系结构设计，细化分层、模块、部署架构。

---

## 一、架构概览

### 1.1 架构风格

**分层架构（Layered Architecture）** + **前后端分离**

```
┌─────────────────────────────────────────────────────────────────────┐
│                           客户端层 (Client Layer)                    │
│  ┌─────────────────────┐        ┌─────────────────────┐             │
│  │   浏览器 - 后台管理   │        │   浏览器 - 前台展示   │             │
│  │   (Vue 3 + EP)      │        │   (Vue 3 + EP)      │             │
│  └──────────┬──────────┘        └──────────┬──────────┘             │
├─────────────┼────────────────────────────────┼───────────────────────┤
│             │         Web 层 (Controller)     │                       │
│             ├─────────────────────────────────┤                       │
│             │  REST API (Spring MVC)          │                       │
│             │  /api/admin/*  后台接口          │                       │
│             │  /api/front/*  前台接口          │                       │
│             └─────────────────────────────────┤                       │
│                                               │                       │
│  ┌────────────────────────────────────────────┴───────────────────┐   │
│  │                    业务逻辑层 (Service Layer)                     │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐    │   │
│  │  │Instructor  │ │  Course    │ │ Category   │ │  Order     │    │   │
│  │  │  Service   │ │  Service   │ │  Service   │ │  Service   │    │   │
│  │  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬──────┘    │   │
│  │        │              │              │              │            │   │
│  │        └──────────────┴──────────────┴──────────────┘            │   │
│  └────────────────────────────────────────────────────────────────┤   │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                   数据访问层 (DAO / Mapper)                     │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │   │
│  │  │UserMapper│ │Instructor│ │ Course   │ │Category  │          │   │
│  │  │          │ │ Mapper   │ │ Mapper   │ │ Mapper   │          │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘          │   │
│  │       └────────────┴────────────┴────────────┘                │   │
│  └───────────────────────────────────────────────────────────────┤   │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │                    数据存储层 (Storage)                        │   │
│  │  ┌───────────────────┐    ┌───────────────────┐               │   │
│  │  │  PostgreSQL 18.x  │    │  Redis (缓存)      │               │   │
│  │  │  主数据库          │    │  可选              │               │   │
│  │  └───────────────────┘    └───────────────────┘               │   │
│  └───────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、模块划分

### 2.1 后端模块结构

```
edu-platform/
├── edu-platform-common/          # 通用模块
│   ├── Result.java               # 统一响应封装
│   ├── ErrorCode.java            # 错误码枚举
│   ├── BusinessException.java    # 业务异常
│   └── ValidationException.java  # 参数校验异常
│
├── edu-platform-api/             # API 接口模块
│   ├── dto/                      # 数据传输对象
│   ├── vo/                       # 视图对象
│   └── query/                    # 查询条件
│
├── edu-platform-service/         # 业务逻辑模块
│   ├── impl/                     # 服务实现
│   └── domain/                   # 领域对象
│
├── edu-platform-persistence/     # 数据访问模块
│   ├── mapper/                   # MyBatis Mapper
│   ├── entity/                   # JPA Entity
│   └── repository/               # Spring Data Repository
│
└── edu-platform-web/             # Web 层模块
    ├── controller/               # REST Controller
    ├── config/                   # 配置类
    └── aspect/                   # 切面（日志、事务）
```

### 2.2 前端模块结构

```
edu-platform-frontend/
├── src/
│   ├── api/                      # HTTP 请求封装
│   │   ├── admin/                # 后台 API
│   │   └── front/                # 前台 API
│   │
│   ├── assets/                   # 静态资源
│   ├── components/               # 公共组件
│   │   ├── admin/                # 后台组件
│   │   └── front/                # 前台组件
│   │
│   ├── router/                   # 路由配置
│   │   ├── admin.ts              # 后台路由
│   │   └── front.ts              # 前台路由
│   │
│   ├── store/                    # Pinia 状态管理
│   │   ├── modules/
│   │   │   ├── auth.ts           # 认证状态
│   │   │   ├── course.ts         # 课程状态
│   │   │   └── instructor.ts     # 讲师状态
│   │   └── index.ts
│   │
│   ├── views/
│   │   ├── admin/                # 后台页面
│   │   │   ├── InstructorManage.vue
│   │   │   ├── CourseManage.vue
│   │   │   └── CategoryManage.vue
│   │   │
│   │   └── front/                # 前台页面
│   │       ├── Index.vue
│   │       ├── CourseList.vue
│   │       └── InstructorList.vue
│   │
│   ├── App.vue
│   └── main.ts
│
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 三、接口设计

### 3.1 REST API 规范

| 方法 | 语义 | 示例 |
|------|------|------|
| `GET` | 查询 | `/api/admin/instructors` |
| `POST` | 创建 | `/api/admin/instructors` |
| `PUT` | 更新 | `/api/admin/instructors/{id}` |
| `DELETE` | 删除 | `/api/admin/instructors/{id}` |

### 3.2 接口清单

#### 后台接口 (`/api/admin/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/instructors` | 讲师列表（分页） |
| GET | `/api/admin/instructors/{id}` | 讲师详情 |
| POST | `/api/admin/instructors` | 创建讲师 |
| PUT | `/api/admin/instructors/{id}` | 更新讲师 |
| DELETE | `/api/admin/instructors/{id}` | 删除讲师 |
| | | |
| GET | `/api/admin/courses` | 课程列表（分页） |
| GET | `/api/admin/courses/{id}` | 课程详情 |
| POST | `/api/admin/courses` | 创建课程 |
| PUT | `/api/admin/courses/{id}` | 更新课程 |
| DELETE | `/api/admin/courses/{id}` | 删除课程 |
| POST | `/api/admin/courses/{id}/publish` | 上架课程 |
| POST | `/api/admin/courses/{id}/unpublish` | 下架课程 |
| | | |
| GET | `/api/admin/categories/tree` | 分类树 |
| POST | `/api/admin/categories` | 创建分类 |
| PUT | `/api/admin/categories/{id}` | 更新分类 |
| DELETE | `/api/admin/categories/{id}` | 删除分类 |

#### 前台接口 (`/api/front/*`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/front/courses` | 课程列表（前台展示） |
| GET | `/api/front/courses/{id}` | 课程详情 |
| GET | `/api/front/courses/search` | 课程搜索 |
| | | |
| GET | `/api/front/instructors` | 讲师列表 |
| GET | `/api/front/instructors/{id}` | 讲师详情（含所授课程） |
| | | |
| GET | `/api/front/categories/tree` | 分类树（前台导航） |

---

## 四、部署架构

### 4.1 开发环境

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   IDEA      │     │  PostgreSQL │     │   Redis     │
│   (IDE)     │     │   (Docker)  │     │  (Docker)   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                    ┌──────┴──────┐
                    │  localhost  │
                    │  :8080      │
                    │  :5432      │
                    │  :6379      │
                    └─────────────┘
```

### 4.2 生产环境（简化版）

```
┌─────────────────────────────────────────────────────────────┐
│                        Nginx (反向代理)                       │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  api.eduplatform.com    │  www.eduplatform.com          │ │
│  │  /api/admin/*           │  / (前端静态资源)              │ │
│  │  /api/front/*           │                                │ │
│  └────────┬────────┘    └─────────────────────────────────┘ │
├───────────┼──────────────────────────────────────────────────┤
│           │            Spring Boot 应用                        │
│           │         (edu-platform-0.0.1-SNAPSHOT.jar)          │
│           └──────────────────┬───────────────────────────────┘
│                              │
├──────────────────────────────┼───────────────────────────────┤
│           ┌──────────────────┴───────────────────────────────┐│
│           │              PostgreSQL 18.x                      ││
│           │              (主从架构，可选)                       ││
│           └──────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────┘
```

### 4.3 Docker Compose（开发环境一键启动）

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:18
    environment:
      POSTGRES_DB: edu_platform
      POSTGRES_USER: edu_user
      POSTGRES_PASSWORD: edu_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## 五、安全设计

### 5.1 认证授权

| 层级 | 方案 | 说明 |
|------|------|------|
| 网关层 | Nginx 限流 | 防止 DDoS |
| 应用层 | Spring Security + JWT | 后台接口认证 |
| 数据层 | PostgreSQL 角色权限 | 应用账号 vs 只读账号 |

### 5.2 接口安全

- **HTTPS**：生产环境强制 HTTPS
- **CORS**：配置允许的前端域名
- **CSRF**：前后端分离可关闭（JWT 认证）
- **XSS**：输入校验 + 输出转义
- **SQL 注入**：MyBatis-Plus 预编译，禁止拼接 SQL

---

## 六、性能设计

| 维度 | 方案 |
|------|------|
| 数据库索引 | 主键、外键、查询字段建索引 |
| 分页查询 | 大表使用 `LIMIT + OFFSET`，可优化为游标分页 |
| 缓存 | Redis 缓存热门课程、分类树 |
| 静态资源 | Nginx 直接托管，CDN 加速 |
| 连接池 | HikariCP 默认配置，连接数 10-20 |

---

## 七、监控与日志

| 组件 | 方案 |
|------|------|
| 应用日志 | Logback + JSON 格式 |
| 日志收集 | ELK Stack（可选） |
| 健康检查 | Spring Boot Actuator `/actuator/health` |
| 接口监控 | Swagger + 自定义拦截器统计响应时间 |

---

*生成时间：2026-11-19 | 基于 Document_Analysis.md*
