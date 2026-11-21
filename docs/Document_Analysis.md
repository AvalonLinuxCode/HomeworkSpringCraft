# Document_Analysis — 工程分析表

> 基于 `docs/客户需求.md` 深度梳理，覆盖需求、架构、设计、开发、分析全生命周期。

---

## 一、项目概览

| 维度 | 内容 |
|------|------|
| **项目名称** | Spring Boot 在线教育平台 |
| **项目类型** | 课程实验 / 软件工程全流程实践 |
| **技术栈** | Spring Boot 3.x + Vue 3 + Element Plus + PostgreSQL + IDEA + draw.io |
| **交付形态** | 需求文档 + UML 模型 + 可运行服务 + 实验报告 |
| **核心目标** | 从需求分析到开发上线的端到端软件工程实践 |

---

## 二、需求分析

### 2.1 功能模块分解

| 模块域 | 子模块 | 功能描述 | 优先级 |
|--------|--------|----------|--------|
| **后台** | 后台登录 | 管理员身份认证与权限入口 | P0 |
| | 讲师管理 | 讲师信息的增删改查（CRUD） | P0 |
| | 课程分类管理 | 课程分类体系的创建、编辑、层级维护 | P0 |
| | 课程管理 | 课程信息维护（标题、描述、价格、关联讲师/分类） | P0 |
| **前台** | 首页展示 | 推荐课程、热门讲师、分类导航 | P1 |
| | 前台登录/注册 | 学员用户注册与登录 | P0 |
| | 讲师列表 / 详情 | 讲师浏览、讲师主页（所授课程等） | P1 |
| | 课程列表 / 详情 | 课程浏览、课程详情页（章节、评价等） | P0 |
| | 课程支付 | 下单、支付流程（可对接模拟支付） | P1 |

### 2.2 非功能需求（隐含）

| 维度 | 要求 | 依据 |
|------|------|------|
| 可维护性 | 分层架构、清晰模块边界 | 软件工程课程目标 |
| 可扩展性 | 课程分类支持层级、讲师/课程可独立管理 | 业务增长预期 |
| 可用性 | 前后端分离，前台/后台独立访问 | 在线教育场景 |
| 安全性 | 登录认证、权限隔离（后台 vs 前台） | 需求文档明确 |

### 2.3 需求交付物清单

| # | 交付物 | 工具 | 对应阶段 |
|---|--------|------|----------|
| 1 | 需求规格说明书 / 用例说明文档 | 文本编辑器 | 需求分析 |
| 2 | 用例图（Use Case Diagram） | draw.io | 需求分析 |

---

## 三、体系结构设计

### 3.1 架构分层

```
┌─────────────────────────────────────────────┐
│                  客户端层                      │
│    浏览器 (前台)  │  浏览器 (后台管理)          │
├─────────────────────────────────────────────┤
│                Web 层 (Controller)            │
│    Spring MVC / REST API                     │
├─────────────────────────────────────────────┤
│                业务逻辑层 (Service)            │
│    讲师服务 · 课程服务 · 分类服务 · 支付服务    │
├─────────────────────────────────────────────┤
│                数据访问层 (DAO / Mapper)       │
│    MyBatis / Spring Data JPA                 │
├─────────────────────────────────────────────┤
│                数据存储层                      │
│    MySQL / Redis                             │
└─────────────────────────────────────────────┘
```

### 3.2 架构交付物

| # | 交付物 | 工具 |
|---|--------|------|
| 3 | 体系结构图 | draw.io |

---

## 四、详细设计

### 4.1 数据库设计分析

#### 核心实体识别（基于功能模块反推）

| 实体 | 关键字段 | 关联实体 | 说明 |
|------|----------|----------|------|
| `user` | id, username, password, email, role, created_at | — | 前台学员 + 后台管理员（role 区分） |
| `instructor` | id, name, avatar, title, description, created_at | courses | 讲师信息 |
| `category` | id, name, parent_id, sort_order, level | courses | 课程分类（支持树形层级） |
| `course` | id, title, description, price, instructor_id, category_id, status, created_at | instructor, category, orders | 课程主表 |
| `order` | id, user_id, course_id, amount, status, pay_time, created_at | user, course | 订单/支付记录 |

#### PostgreSQL 特性利用（可选增强）

| 特性 | 应用场景 | 示例 |
|------|----------|------|
| JSONB | 课程详情（动态字段：章节、评价、标签） | `course.details` 存 JSON |
| 数组 | 课程标签 | `course.tags TEXT[]` |
| 枚举 | 状态字段（status） | `course_status` ENUM |
| 全文搜索 | 课程搜索（标题 + 描述） | `tsvector` 索引 |
| 树形查询 | 分类层级（递归） | `WITH RECURSIVE` |

> 注：基础版本用标准字段即可，上述特性可作为扩展加分项。

#### 表关系

```
user          1:N  order
  │
  └── role: 'admin' / 'student'

instructor    1:N  course

category      1:N  category (自引用 parent_id)
    │
    └── 1:N  course

course        1:N  order
```

### 4.2 构件级设计（服务拆分）

| 服务 | 职责 | 接口示例 |
|------|------|----------|
| `InstructorService` | 讲师 CRUD | `list`, `getById`, `create`, `update`, `delete` |
| `CourseService` | 课程 CRUD + 上下架 | `list`, `getById`, `search`, `publish`, `unpublish` |
| `CategoryService` | 分类管理（含树形） | `listTree`, `add`, `update`, `delete` |
| `OrderService` | 订单创建与支付 | `create`, `pay`, `queryStatus` |
| `AuthService` | 登录注册（**不选**） | — |

> **开发范围约束**：需求明确要求"挑选登录以外的 1~2 个业务服务功能"，因此 `AuthService` 不纳入开发，优先选 `InstructorService` + `CourseService`。

### 4.3 UML 模型交付物

| # | 交付物 | 工具 | 类型 |
|---|--------|------|------|
| 4 | 类图（Class Diagram） | IDEA 插件自动生成 | 静态结构 |
| 5 | E-R 图 | draw.io | 数据模型 |
| 6 | 活动图（Activity Diagram） | draw.io | 业务流程 |

---

## 五、开发计划

### 5.1 服务开发范围

| 服务 | 是否开发 | 理由 |
|------|----------|------|
| 登录 / 注册 | ❌ 不开发 | 需求明确排除 |
| 讲师管理 | ✅ 开发 | 核心业务，CRUD 完整，易验证 |
| 课程管理 | ✅ 开发 | 核心业务，与讲师/分类关联，展示面广 |
| 课程分类管理 | ⚠️ 可选 | 依赖课程管理，可合并到课程服务 |
| 课程支付 | ⚠️ 可选 | 涉及第三方对接，可用模拟实现 |

### 5.2 开发任务分解

| 阶段 | 任务 | 产出 |
|------|------|------|
| 环境搭建 | Spring Boot 项目初始化、依赖配置 | 可运行空项目 |
| 数据层 | 建表 SQL、MyBatis Mapper / JPA Repository | 数据库连通 |
| 业务层 | InstructorService + CourseService 实现 | 服务接口可用 |
| 接口层 | REST Controller（前台 + 后台） | API 可调用 |
| 部署 | 打包、部署、验证 | 上线服务 |

### 5.3 技术选型（已确定）

#### 后端

| 技术 | 选型 | 理由 |
|------|------|------|
| 框架 | Spring Boot 3.x | 需求指定 |
| 数据库 | PostgreSQL 18.x | 开源纯粹(BSD/MIT)、ACID严格、JSONB支持、Spring Boot原生 |
| ORM | MyBatis-Plus | 简化 CRUD，适合教学 |
| 构建 | Maven | 标准 Java 项目构建 |
| API 文档 | Swagger / OpenAPI | 接口可视化 |

#### 前端（Vue 3 技术栈）

| 技术 | 选型 | 理由 |
|------|------|------|
| 框架 | Vue 3（Composition API） | 中文生态成熟，学习成本低 |
| 构建工具 | Vite | 秒级热更新，开发体验好 |
| UI 组件库 | Element Plus | 表格 / 表单 / 弹窗开箱即用，后台 CRUD 提效显著 |
| 路由 | Vue Router 4 | 官方标准路由方案 |
| 状态管理 | Pinia | 替代 Vuex，更简洁的响应式状态 |
| HTTP 请求 | Axios | 封装拦截器、统一错误处理 |
| 语言 | JavaScript（可选 TS） | 作业场景用 JS 即可，降低上手成本 |

**选型结论：** 后台管理与前台展示复用同一套 Vue 3 + Element Plus 技术栈，前后端通过 REST API 分离，前端构建产物由 Nginx 托管或直接放入 Spring Boot `static` 目录。

---

## 六、结果分析框架

### 6.1 分析维度

| 维度 | 分析内容 |
|------|----------|
| 需求覆盖度 | 各模块是否被完整建模与实现 |
| 架构合理性 | 分层是否清晰、耦合度是否可控 |
| 模型一致性 | 用例图→类图→E-R 图→代码 是否逐层对齐 |
| 设计完整性 | 是否遗漏关键实体、关系、边界场景 |
| 开发可验证性 | 所选服务是否可独立测试、部署、演示 |

### 6.2 实验报告素材索引

| 报告章节 | 对应交付物 |
|----------|------------|
| 需求分析 | 需求规格说明书 + 用例图 |
| 体系结构设计 | 体系结构图 |
| 详细设计 | 类图 + E-R 图 + 活动图 |
| 数据库设计 | E-R 图 + 建表 SQL |
| 服务开发 | 代码 + API 文档 + 部署记录 |
| 结果分析 | 本文档 + 各模型对比分析 |

---

## 七、风险与注意事项

| 风险 | 影响 | 应对 |
|------|------|------|
| 需求范围膨胀 | 无法按时完成开发 | 严格锁定 1~2 个服务，登录排除 |
| UML 工具不熟 | 图质量不达标 | 提前熟悉 draw.io 模板与形状 |
| 数据库设计返工 | 前后端联调阻塞 | 先定 E-R 图再写代码 |
| 类图自动生成失败 | 设计文档缺失 | 确保 IDEA 插件安装正确，代码先编译通过 |

---

## 八、交付物总览

### 8.1 目录结构

```
docs/
├── Document_Analysis.md          # 工程分析表（本文档）
├── Design_Description/           # 设计文档目录
│   ├── schema.sql                # 数据库 DDL
│   ├── component_design.md       # 构件级设计文档
│   └── architecture_design.md    # 体系结构设计文档
└── model_diagram/                # 模型图目录
    └── README.md                 # 模型图清单与绘制指南
```

### 8.2 交付物清单

| # | 交付物 | 路径 | 状态 |
|---|--------|------|------|
| 1 | 需求规格说明书 | `需求规格说明书.md` | ⬜ 待编写 |
| 2 | 用例图 | `model_diagram/use_case.drawio` | ⬜ 待绘制 |
| 3 | 体系结构图 | `model_diagram/architecture.drawio` | ⬜ 待绘制 |
| 4 | 类图 | `model_diagram/class_diagram.drawio` | ⬜ 待生成 |
| 5 | E-R 图 | `model_diagram/er_diagram.drawio` | ⬜ 待绘制 |
| 6 | 活动图 | `model_diagram/activity_diagram.drawio` | ⬜ 待绘制 |
| 7 | 数据库 DDL | `Design_Description/schema.sql` | ✅ 已完成 |
| 8 | 构件级设计 | `Design_Description/component_design.md` | ✅ 已完成 |
| 9 | 体系结构设计 | `Design_Description/architecture_design.md` | ✅ 已完成 |
| 10 | 服务代码 | `src/` | ⬜ 待开发 |
| 11 | 实验报告 | `实验报告.md` | ⬜ 待编写 |
| 12 | **工程分析表** | `Document_Analysis.md` | ✅ 已完成 |


*生成时间：2026-11-19 | 基于 `docs/客户需求.md` 深度分析 | 数据库已确认为 PostgreSQL | 设计文档已创建*

