# 在线教育平台（EduPlatform）

> 软件工程课程实验项目 — Spring Boot 3 + Vue 3 前后端分离的在线教育平台，
> 覆盖需求分析 → 设计建模 → 开发 → 测试 → 部署全流程实践。

> [!IMPORTANT]
> **克隆后请先读这段——文档里的路径和端口是原作者本机的，不是通用值。**
>
> - **路径**：部分文档（尤其 `guide.md` 的示例、历史开发日志）出现 `E:\AllProject\...`、`D:\myDB\...` 等绝对路径，这些是**原作者开发机的真实路径，直接照搬会失败**。请把它们当作「占位示例」，替换成你自己的克隆目录与 PostgreSQL 安装目录。`start.bat` 已用 `%~dp0` 自动定位脚本所在目录，**跨机器免配置**。
> - **端口**：PostgreSQL 默认是 **5432**，本项目用的是 **5433**。若你的 PG 在 5432（或其它端口），无需改代码，设置环境变量 `DB_URL` / `TEST_DB_URL` 即可。
> - **前置布局**：`docs/` 目录必须与 `edu-platform/` 保持同级（默认克隆即满足），否则 `pom.xml` 构建期按相对路径拷贝 `schema.sql` 会失败。
>
> 完整说明见 **[guide.md](guide.md)**「变量约定」与「常见问题」。
>
> 也可已将 **[guide.md](guide.md)** 丢给你的Agent，叫他帮你部署

## ✨ 功能概览

| 模块 | 功能 |
|------|------|
| 后台管理 | 讲师管理、课程管理（含上下架）、分类管理 |
| 前台展示 | 首页、课程浏览 / 详情、讲师浏览 / 详情、课程搜索 |

已落地垂直切片：**InstructorService、CourseService、CategoryService**（不含认证，需求明确排除）。

## 🧱 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 · Vite · Element Plus · Pinia · TypeScript |
| Web/业务 | Spring Boot 3.5 · 统一 `Result` 封装 · springdoc-openapi（Swagger UI） |
| 持久层 | MyBatis-Plus 3.5 · PostgreSQL 18（全文检索） |
| 测试 | JUnit 5 + Mockito（Ring A 单元）· Spring Boot Test + 真实 PostgreSQL（Ring B 集成） |

## 📁 目录结构

```
.
├── edu-platform/              # 后端（Maven，Java 21）
│   ├── src/main/java/com/eduplatform/
│   │   ├── controller/        # admin/ 后台接口 · front/ 前台接口
│   │   ├── service/impl/      # 业务实现
│   │   ├── mapper/            # MyBatis-Plus Mapper
│   │   ├── entity/ dto/ vo/   # 实体 / 入参 / 出参
│   │   └── common/ config/    # 通用封装 / 配置
│   └── src/test/              # Ring A 单元 + Ring B 集成测试
├── edu-platform-frontend/     # 前端（Vite + Vue 3）
│   └── src/{api,views/admin,views/front,router,store,types}/
├── docs/                      # 需求 / 设计 / UML 模型图 / 实验报告 / 交付清单
├── guide.md                   # 本地环境安装与部署指南
└── start.bat                  # Windows 一键启动前后端
```

## 🚀 快速开始

环境要求：**JDK 17+（编译目标 21）· Maven 3.8+ · Node.js 18+ · PostgreSQL 18**。
> PostgreSQL 本项目用 **5433** 端口（非默认 5432）；若你的实例端口不同，设环境变量 `DB_URL` / `TEST_DB_URL` 即可，无需改代码。

```cmd
:: 1) 一次性准备数据库（建 edu_user / edu_platform / edu_platform_test + 导入 DDL）
::    详见 guide.md「任务 2：数据库准备」——注意 docs/ 必须与 edu-platform/ 同级

:: 2) 前端安装依赖（首次）
cd edu-platform-frontend && npm install

:: 3) 一键启动（双击运行，或在 cmd.exe 中执行）
start.bat
```

脚本会自动预检环境与端口，先后端（8080）后前端（5173）分窗口启动。手动启动与排错见 **[guide.md](guide.md)**。

| 入口 | 地址 |
|------|------|
| 前台 | http://localhost:5173/ |
| 后台管理 | http://localhost:5173/admin/instructors |
| API 文档 | http://localhost:8080/swagger-ui/index.html |

> ⚠️ Windows 下请在 **cmd.exe** 中执行 `mvn` / `start.bat`（Git Bash 中 Maven 脚本会死循环，见 guide.md FAQ #1）。

## 🔌 API 一览

```
# 后台
GET/POST/PUT/DELETE  /api/admin/instructors[/{id}]
GET/POST/PUT/DELETE  /api/admin/courses[/{id}]
POST                 /api/admin/courses/{id}/publish · /unpublish
GET/POST/PUT/DELETE  /api/admin/categories[/{id}]
GET                  /api/admin/categories/tree

# 前台
GET  /api/front/courses?keyword=&pageNum=&pageSize= · /api/front/courses/{id}
GET  /api/front/instructors · /api/front/instructors/{id}
```

## 🧪 测试

```cmd
cd edu-platform
mvn test      :: Ring A 单元测试（25 个，不依赖数据库）
mvn verify    :: Ring A + Ring B 集成测试（22 个，连本机 edu_platform_test 库，每例重放 schema.sql）
```

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [docs/客户需求.md](docs/客户需求.md) | 原始需求 |
| [docs/Document_Analysis.md](docs/Document_Analysis.md) | 工程分析表 |
| [docs/Design_Description/](docs/Design_Description/) | 体系结构 / 构件设计 / `schema.sql`（DDL 唯一事实来源） |
| [docs/model_diagram/](docs/model_diagram/) | 用例图 / 架构图 / E-R 图 / 时序图 / 类图（SVG） |
| [docs/实验报告.md](docs/实验报告.md) · [docs/交付清单.md](docs/交付清单.md) | 实验报告与交付物清单 |
| [guide.md](guide.md) | 环境安装、数据库准备、启动/停止、常见问题 |

## 📄 License

[MIT](LICENSE)
