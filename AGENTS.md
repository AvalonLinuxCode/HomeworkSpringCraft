# Repository Guidelines

> Spring Boot 在线教育平台 — 软件工程课程实验项目

---

## Project Overview

**目的**：完成在线教育平台从需求分析到开发上线的全流程软件工程实践。

**当前状态**：🟢 v0.2.1-SNAPSHOT — 讲师 / 课程 / 分类三服务与 Vue 3 前后端全部落地；单元 44/44、集成 25/25 全绿；审计 04 的 2 BLOCKER + 4 MAJOR + 7 MINOR + 5 NIT 已整改，复审轮又补了 3 MAJOR + 2 个新缺陷（后台课程详情端点缺失、未映射路径返回 500），处置见 `CodeLog/05` 第七节。

**核心功能**：
- 后台：讲师管理、课程管理、分类管理
- 前台：课程浏览、讲师浏览、课程搜索

**技术栈**：Spring Boot 3.5.16 + MyBatis-Plus 3.5.12 + PostgreSQL 18.x（本地实例，127.0.0.1:5433） + Vue 3 + Element Plus + Vite 5

**范围**：只实现了 InstructorService、CourseService、CategoryService 三条纵向切片。AuthService 需求明确排除，无认证。

---

## Architecture & Data Flow

### 架构风格

**分层架构** + **前后端分离**。后端三层：Controller（REST）→ Service（业务）→ Mapper（MyBatis-Plus 持久层）；持久层契约是 `src/main/resources/mapper/*.xml`，**不是 JPA**。

```
Vue 3 客户端 (axios, baseURL=/api)
    ↓ REST (/api/admin/*, /api/front/*)
Controller (@RestController, 构造器注入 Service, 返回 Result<...>)
    ↓
Service 接口 + Impl (@Transactional)
    ↓
Mapper (MyBatis-Plus BaseMapper + XML)
    ↓
PostgreSQL (127.0.0.1:5433)
```

### 前后端联调

- 开发期：Vite dev server (5173) 把 `/api` 代理到 `127.0.0.1:8080`（见 `vite.config.ts`），同源无跨域。
- 生产：前端 `client.ts` 用相对路径 `baseURL:'/api'`，由后端或反代同域承接。

### 三条纵向切片的数据流

```
InstructorController → InstructorService → InstructorMapper → PostgreSQL
CourseController    → CourseService    → CourseMapper    → PostgreSQL
                         ↘ InstructorMapper（关联讲师）/ CategoryMapper（关联分类）
CategoryController  → CategoryService  → CategoryMapper  → PostgreSQL
```

课程搜索走 PostgreSQL 全文检索：`title` 用 `to_tsvector('simple', ...) @@ plainto_tsquery(...)`，`description` 用 `ILIKE '%kw%'` + `pg_trgm` GIN 索引（见 `CourseMapper.xml`）。

---

## Key Directories

```
edu-platform/                     # 后端（Maven）
├── src/main/java/com/eduplatform/
│   ├── EduPlatformApplication.java   # 启动类，@MapperScan("com.eduplatform.mapper")
│   ├── common/                       # Result, PageResult, 异常体系 + GlobalExceptionHandler
│   ├── config/                       # WebConfig(CORS), OpenApiConfig
│   ├── controller/admin/             # Course/Instructor/Category AdminController（/api/admin/*）
│   ├── controller/front/             # Course/Instructor FrontController（/api/front/*）
│   ├── service/ + service/impl/      # 接口 + 实现（@Transactional）
│   ├── mapper/ + mapper/handler/     # MyBatis-Plus Mapper + StringArrayTypeHandler(JSONB/数组)
│   ├── entity/                       # Course, Instructor, Category（MyBatis-Plus 实体，非 JPA）
│   ├── dto/                          # CourseDTO, InstructorDTO, CategoryDTO, CourseQuery, InstructorQuery
│   └── vo/                           # CourseVO, CourseDetailVO, InstructorVO, CategoryVO
├── src/main/resources/
│   ├── application.yml               # 数据源用 DB_URL/DB_USER/DB_PASSWORD 环境变量占位
│   └── mapper/*.xml                  # 持久层契约（MyBatis XML）
├── src/test/java/com/eduplatform/    # Ring A 单元测试(*Test) + Ring B 集成测试(*IT)
└── pom.xml

edu-platform-frontend/             # 前端（Vite + Vue 3 + TS + Element Plus + Pinia）
└── src/
    ├── api/                          # client.ts(axios 实例) + course/instructor/category.ts
    ├── views/admin/                  # CourseManage, InstructorManage, CategoryManage
    ├── views/front/                  # Home, CourseList, CourseDetail, InstructorList, InstructorDetail
    ├── layouts/                      # AdminLayout, FrontLayout
    ├── router/index.ts               # 路由
    ├── store/                        # Pinia 状态
    └── types/index.ts                # ApiResult 等类型

docs/                              # 设计文档（唯一 DDL 在 Design_Description/schema.sql）
├── Document_Analysis.md              # 工程分析表
├── 客户需求.md / 交付清单.md / 实验报告.md
├── Design_Description/               # schema.sql + architecture_design.md + component_design.md
└── model_diagram/                    # use_case / architecture / class / sequence / ER 的 .svg

CodeLog/                           # 本地审计日志（gitignore，不入库）
```

**数据库 DDL 只有一份**：`docs/Design_Description/schema.sql`。`pom.xml` 的 `maven-antrun-plugin` 在 `initialize` 阶段把它复制到测试类路径 `src/test/resources/schema.sql`，**不要在模块内另建 schema.sql**。`docs/` 必须与 `edu-platform/` 同级（antrun 按相对路径 `../docs/...` 复制）。

---

## Development Commands

### 后端（Spring Boot, JDK 21）

```bash
cd edu-platform
mvn clean package -DskipTests      # 打包（产物 target/edu-platform-1.0.0-SNAPSHOT.jar）
mvn spring-boot:run                # 本地启动（默认 8080）
mvn test                           # Ring A：单元测试，无数据库依赖
mvn verify                         # Ring A + Ring B：集成测试，连本地 PG 的 edu_platform_test
mvn test -Dtest=CourseServiceImplTest      # 跑单个单元测试
```

### 前端（Vue 3, Node 18+）

```bash
cd edu-platform-frontend
npm install
npm run dev                        # 开发服务器 5173，/api 代理到 8080
npm run build                      # 类型检查(vue-tsc) + 生产构建 → dist/
npm run type-check                 # 仅 vue-tsc --noEmit
npm run lint
```

### 一键启动（Windows）

根目录 `start.bat`：自动定位 `%~dp0`，检查 `mvn/node/npm` 与端口占用，起后端(8080)与前端(5173)。

### 数据库（PostgreSQL 18，本机 5433）

```bash
# 首次准备（guide.md 有完整版；本机实例装在 5433 而非 5432）
psql -h 127.0.0.1 -p 5433 -U postgres -c "CREATE ROLE edu_user LOGIN PASSWORD 'edu_password';"
psql -h 127.0.0.1 -p 5433 -U postgres -c "CREATE DATABASE edu_platform OWNER edu_user;"
psql -h 127.0.0.1 -p 5433 -U postgres -c "CREATE DATABASE edu_platform_test OWNER edu_user;"

# 执行唯一 DDL
psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -f docs/Design_Description/schema.sql
```

> **Windows / msys2 注意**：Git Bash（msys2）下 `mvn` 会死循环、`psql.exe` 无法用 `/d/...` 路径启动。要么用 PowerShell / cmd.exe，要么 `export MAVEN_BASEDIR='E:\...\edu-platform'` 后再调 mvn；psql 用 `D:/...` Windows 路径或先 cd 进 bin 目录调 `./psql.exe`。

---

## Code Conventions & Common Patterns

### 命名规范

| 类型 | 规范 | 示例 |
|---|---|---|
| 类名 | PascalCase | `CourseServiceImpl` |
| 方法名 | camelCase | `listCourses()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 包名 | 全小写 | `com.eduplatform.service` |
| 测试 | `*Test`(单元) / `*IT`(集成) | `CourseServiceImplTest` / `CourseMapperIT` |

### 分层模式（无 Lombok，Java 21）

```java
// Controller：@RestController + 构造器注入 + Swagger @Tag/@Operation + 返回 Result<...>
@RestController
@RequestMapping("/api/admin/courses")
public class CourseAdminController {
    private final CourseService courseService;
    public CourseAdminController(CourseService courseService) { this.courseService = courseService; }

    @GetMapping
    public Result<PageResult<CourseVO>> list(CourseQuery query) {
        return Result.success(courseService.list(query));
    }
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publish(id); return Result.success(null);
    }
}

// Service 接口 + 实现；写操作加 @Transactional(rollbackFor = Exception.class)
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;
    // 构造器注入；删除时 deleteById 返回 0 视为“不存在”，手动抛 NotFoundException
}

// Mapper：继承 MyBatis-Plus BaseMapper，复杂 SQL 写在 resources/mapper/*.xml
public interface CourseMapper extends BaseMapper<Course> { ... }
```

### 统一响应与异常

- **`Result<T>`** 包装所有响应：`{code, message, data}`，成功 `code=200`。
- **`PageResult<T>`** 承载分页：`{list, total, pageNum, pageSize}`。
- **异常体系**（`common/`）：`ApplicationException` 为基，派生 `BusinessException`(400)、`ValidationException`(400)、`NotFoundException`(404)；`GlobalExceptionHandler`(`@RestControllerAdvice`) 统一转成 `Result` 与 HTTP 状态码，未映射路径 → 404（`NoResourceFoundException`）。
- 前端 `client.ts` 拦截器自动解包 `Result`：`code!==200` 时 `ElMessage.error` 并 reject，成功直接返回 `data`。

### 持久层约定（MyBatis-Plus，易踩坑）

- **`FieldStrategy.ALWAYS`**：更新时把 null 也写入 DB（否则 MP 默认忽略 null 字段，无法清空列）。
- **`deleteById` 返回 0** 表示未删到行（不是抛异常），Service 层要判断返回值。
- **分页参数**用包装类型（`Integer`），原始 `int` 会绕过归一化，导致 pageNum/pageSize 缺省值失效。
- **JSONB / 数组列**用 `StringArrayTypeHandler`；**非法 JSONB 载荷必须在 Service 层先校验**，否则 DB `CAST` 报错。
- **`@Sql` 会破坏 `$$` 函数体**：schema.sql 里含 `DO $$ ... $$`，集成测试不要用 `@Sql` 整体回放，需用手工 `Statement.execute` 拆分执行。

---

## Important Files

| 文件 | 用途 | 优先级 |
|---|---|---|
| `docs/Design_Description/schema.sql` | 唯一 DDL，建表/枚举/索引（含 pg_trgm DO 块） | 🔴 高 |
| `edu-platform/src/main/resources/application.yml` | 数据源(env 占位)、sql.init.mode=never、mapper-locations | 🔴 高 |
| `edu-platform/src/main/resources/mapper/*.xml` | 持久层 SQL 契约 | 🔴 高 |
| `edu-platform/pom.xml` | 构建/插件（antrun schema 拷贝、failsafe IT） | 🔴 高 |
| `docs/Document_Analysis.md` | 工程分析表 | 🟡 中 |
| `edu-platform-frontend/src/api/client.ts` | axios 实例，baseURL=/api，Result 解包 | 🟡 中 |
| `start.bat` / `guide.md` | Windows 一键启动 / 本地部署指南 | 🟡 中 |

---

## Runtime/Tooling Preferences

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | **21**（必须，17 起不来） | 后端 |
| Maven | 3.8+ | 构建 |
| Spring Boot | 3.5.16 | 应用框架 |
| MyBatis-Plus | 3.5.12 | ORM |
| PostgreSQL | 18.x（已在 18.7 验证） | 本机端口 **5433** |
| Node.js | 18+ | 前端 |
| Vite | 5.x | 前端构建 |
| Element Plus | 2.x | UI 组件 |
| Pinia | 2.x | 状态管理 |

**环境变量覆盖**（不改 application.yml 即可切环境）：`DB_URL`、`DB_USER`、`DB_PASSWORD`；`sql.init.mode=never`，启动**绝不自动建表**，DDL 只能手工/测试执行。

---

## Testing & QA

### 两环测试策略

| 环 | 范围 | 命令 | 依赖 |
|---|---|---|---|
| **Ring A** 单元 | Service 业务规则、Controller 绑定语义、异常分支 | `mvn test` | 无 DB，Mockito / standalone MockMvc |
| **Ring B** 集成 | Mapper + DDL，真实 SQL | `mvn verify` | 本地 PG 18 的 `edu_platform_test`，重放 schema.sql |

- **Ring A**（`*Test.java`，Surefire）：`InstructorServiceImplTest`、`CourseServiceImplTest`、`CategoryServiceImplTest`（`@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks`）；`AdminQueryBindingTest`（standalone MockMvc，验证分页默认值、非法 status→400、超限 pageSize、404/500 映射）；`GlobalExceptionHandlerTest`（纯 new，验证异常→HTTP 状态映射）。**当前 44/44 全绿。**
- **Ring B**（`*IT.java`，Failsafe 在 `verify` 阶段跑）：`CourseMapperIT`、`InstructorMapperIT`、`CategoryMapperIT` 等，连 `jdbc:postgresql://127.0.0.1:5433/edu_platform_test`，每用例重放 `schema.sql`。**当前 25/25 全绿。** 运行前先重建 jar（`mvn clean package -DskipTests`）再做运行时探针。
- 无 jacoco 插件（当前未配覆盖率门禁）。

### 用例设计要点

- 契约测试优先覆盖**消费者可见**的行为（分页默认、错误码、状态映射），不做 tautology / 裸不抛异常测试。
- 集成用例确定性、隔离、可全量跑（依赖独立 `edu_platform_test` 库，不污染开发库）。

---

## 已知债务 / 注意事项

- `pg_trgm` 对 1-2 字符关键词效果差（ILIKE 三元组下限）。
- `updateCourse` 存在 lost-update 窗口（无乐观锁）。
- `/api/front/categories/tree` 未实现。
- Swagger 注解不全（部分 Controller 缺 `@Operation`）。
- `Linux-guide.md` 是**未应用的部署提案**：其提议的 `SPAConfig.java` 目前磁盘上不存在，`WebConfig.java` 只配了 CORS；采纳前先核对。
- `CodeLog/` 为本地审计记录，已 gitignore，不入库。

---

*基于四路并行代码勘察综合整理（core src / tests / configs-build / docs-scripts），对照当前磁盘实况校订。*
