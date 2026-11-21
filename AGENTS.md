# Repository Guidelines

> Spring Boot 在线教育平台 — 软件工程课程实验项目

---

## Project Overview

**目的**：完成在线教育平台从需求分析到开发上线的全流程软件工程实践。

**当前状态**：🟢 设计文档完成；讲师服务（InstructorService）垂直切片已落地，8 个单元测试 + 6 个集成测试全绿，其余服务待开发。

**核心功能**：
- 后台：讲师管理、课程管理、分类管理
- 前台：课程浏览、讲师浏览、课程搜索

**技术栈**：Spring Boot 3.x + Vue 3 + Element Plus + PostgreSQL 18.x（本地实例，127.0.0.1:5433）

---

## Architecture & Data Flow

### 架构风格

**分层架构** + **前后端分离**

```
客户端 (Vue 3)
    ↓ REST API
Web 层 (Controller)
    ↓
业务逻辑层 (Service)
    ↓
数据访问层 (Mapper)
    ↓
数据存储层 (PostgreSQL)
```

### 数据流

1. **讲师管理流程**：
   ```
   InstructorController → InstructorService → InstructorMapper → PostgreSQL
   ```

2. **课程管理流程**：
   ```
   CourseController → CourseService → CourseMapper → PostgreSQL
                         ↓
                  InstructorMapper (关联讲师)
                  CategoryMapper (关联分类)
   ```

3. **课程搜索流程**：
   ```
   CourseController → CourseService → search() → PostgreSQL 全文搜索
   ```

---

## Key Directories

### 当前目录结构（设计阶段）

```
docs/
├── Document_Analysis.md          # 工程分析表（主文档）
├── 客户需求.md                    # 原始需求
├── Design_Description/           # 设计文档
│   ├── schema.sql                # 数据库 DDL
│   ├── component_design.md       # 构件级设计
│   └── architecture_design.md    # 体系结构设计
└── model_diagram/                # UML 模型图
    └── README.md                 # 绘制指南
```

### 目录结构（开发阶段）

```
edu-platform/                     # 后端项目（讲师服务切片已落地）
├── src/main/java/com/eduplatform/
│   ├── controller/               # REST Controller（待创建）
│   ├── service/                  # 业务接口
│   │   └── impl/                 # 业务实现
│   ├── mapper/                   # MyBatis Mapper
│   ├── entity/                   # MyBatis 实体（POJO，非 JPA）
│   ├── dto/                      # 数据传输对象
│   ├── vo/                       # 视图对象
│   ├── common/                   # 通用工具
│   └── config/                   # 配置类（待创建）
├── src/main/resources/
│   ├── application.yml           # 配置文件
│   └── mapper/                   # MyBatis XML
├── src/test/java/com/eduplatform/
│   ├── service/                  # Ring A：Mockito 单元测试（mvn test）
│   └── mapper/                   # Ring B：真实 PostgreSQL 集成测试（mvn verify）
└── pom.xml
```

数据库 DDL 只有一份：`docs/Design_Description/schema.sql`，Maven 在构建期复制到测试类路径，**不要在模块内另建 schema.sql**。

```
edu-platform-frontend/            # 前端项目
├── src/
│   ├── api/                      # HTTP 请求封装
│   ├── views/                    # 页面组件
│   ├── store/                    # Pinia 状态
│   └── router/                   # 路由配置
├── package.json
└── vite.config.ts
```

---

## Development Commands

### 后端（Spring Boot）

```bash
# 项目初始化
mvn archetype:generate -DgroupId=com.eduplatform -DartifactId=edu-platform

# 启动应用
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行测试
mvn test

# 生成 API 文档
# 访问 http://localhost:8080/swagger-ui.html
```

### 前端（Vue 3）

```bash
# 创建项目
npm create vue@latest edu-platform-frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建
npm run build

# 代码检查
npm run lint
```

### 数据库（PostgreSQL）

```bash
# 本地 PostgreSQL 18 实例（已安装，端口 5433，数据目录 D:/myDB/postgresql_Data/data）
# 首次准备：建业务账号 + 开发库 + 独立测试库
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE ROLE edu_user LOGIN PASSWORD 'edu_password';"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform OWNER edu_user;"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform_test OWNER edu_user;"

# 执行 DDL（唯一事实来源）
psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -f docs/Design_Description/schema.sql

# 连接数据库
psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform
```

也可用 DBX 连接：host `127.0.0.1`、port `5433`、用户 `postgres` / `edu_user`。

---

## Code Conventions & Common Patterns

### Java 命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 类名 | PascalCase | `InstructorService` |
| 方法名 | camelCase | `listInstructors()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 参数 | camelCase | `instructorId` |
| 包名 | lowercase | `com.eduplatform.service` |

### 分层架构模式

```java
// Controller 层
@RestController
@RequestMapping("/api/admin/instructors")
public class InstructorController {
    @Autowired
    private InstructorService instructorService;
    
    @GetMapping
    public Result<PageResult<InstructorVO>> list(InstructorQuery query) {
        return Result.success(instructorService.list(query));
    }
}

// Service 层
public interface InstructorService {
    PageResult<InstructorVO> list(InstructorQuery query);
}

@Service
public class InstructorServiceImpl implements InstructorService {
    @Autowired
    private InstructorMapper instructorMapper;
    
    @Override
    @Transactional
    public Long create(InstructorDTO dto) {
        // 参数校验
        if (dto.getName() == null) {
            throw new ValidationException("讲师姓名不能为空");
        }
        Instructor entity = dto.toEntity();
        instructorMapper.insert(entity);
        return entity.getId();
    }
}

// Mapper 层
public interface InstructorMapper {
    List<InstructorVO> list(InstructorQuery query);
    int insert(Instructor entity);
    int update(Instructor entity);
    int delete(Long id);
}
```

### DTO/VO 模式

```java
// DTO - 入参
@Data
public class InstructorDTO {
    private String name;
    private String title;
    private String description;
}

// VO - 出参
@Data
public class InstructorVO {
    private Long id;
    private String name;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
```

### 异常处理模式

```java
// 统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public Result<?> handleNotFound(NotFoundException e) {
        return Result.error(404, e.getMessage());
    }
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(400, e.getMessage());
    }
}
```

### 事务管理

```java
@Service
@Transactional
public class CourseServiceImpl implements CourseService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new NotFoundException("课程不存在");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        courseMapper.update(course);
    }
}
```

---

## Important Files

| 文件 | 用途 | 优先级 |
|------|------|--------|
| `docs/Document_Analysis.md` | 工程分析表，项目核心文档 | 🔴 高 |
| `docs/Design_Description/schema.sql` | 数据库 DDL，建表语句 | 🔴 高 |
| `docs/Design_Description/component_design.md` | 构件级设计，服务接口定义 | 🔴 高 |
| `docs/Design_Description/architecture_design.md` | 体系结构设计，接口清单 | 🔴 高 |
| `docs/客户需求.md` | 原始需求文档 | 🟡 中 |
| `docs/model_diagram/README.md` | UML 模型绘制指南 | 🟡 中 |

---

## Runtime/Tooling Preferences

### 后端

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.8+ | 项目构建 |
| Spring Boot | 3.x | 应用框架 |
| PostgreSQL | 16.x | 数据库 |
| MyBatis-Plus | 3.5+ | ORM 框架 |

### 前端

| 工具 | 版本 | 用途 |
|------|------|------|
| Node.js | 18+ | 运行环境 |
| Vue | 3.x | 前端框架 |
| Vite | 4.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Pinia | 2.x | 状态管理 |

### 开发工具

| 工具 | 用途 |
|------|------|
| IDEA | Java IDE |
| VS Code / WebStorm | 前端开发 |
| draw.io | UML 建模 |
| Docker | 容器化部署 |
| Git | 版本控制 |

### 配置文件

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/edu_platform
    username: edu_user
    password: edu_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.eduplatform.entity

server:
  port: 8080
```

---

## Testing & QA

### 测试策略

| 层级 | 范围 | 工具 |
|------|------|------|
| 单元测试 | Service 层方法 | JUnit 5 + Mockito |
| 集成测试 | Mapper + DDL | Spring Boot Test + 本地 PostgreSQL 18（独立库 `edu_platform_test`，每个用例重放 `schema.sql`） |
| API 测试 | REST 接口 | Postman / Swagger |

### 测试命令

```bash
# 运行所有测试
mvn test

# 单元测试 + 集成测试（Ring B 连本地 PostgreSQL 18 的 edu_platform_test 库）
mvn verify

# 运行单个测试类
mvn test -Dtest=InstructorServiceTest

# 生成测试覆盖率报告
mvn verify jacoco:report
```

### 测试用例设计

```java
@SpringBootTest
class InstructorServiceTest {
    
    @Autowired
    private InstructorService instructorService;
    
    @Test
    void testCreate_Instructor_Success() {
        InstructorDTO dto = new InstructorDTO();
        dto.setName("张老师");
        dto.setTitle("资深讲师");
        
        Long id = instructorService.create(dto);
        
        assertThat(id).isNotNull();
    }
    
    @Test
    void testCreate_Instructor_WithNullName_ShouldThrow() {
        InstructorDTO dto = new InstructorDTO();
        dto.setName(null);
        
        assertThatThrownBy(() -> instructorService.create(dto))
            .isInstanceOf(ValidationException.class);
    }
}
```

### 质量检查清单

- [ ] 所有 Service 方法有单元测试
- [ ] 接口有 Swagger 文档
- [ ] 数据库操作有事务管理
- [ ] 异常有统一处理
- [ ] 代码有 CheckStyle 检查

---

## Development Scope

### 必须开发（P0）

| 服务 | 接口数 | 优先级 |
|------|--------|--------|
| InstructorService | 6 | 🔴 高 |
| CourseService | 7 | 🔴 高 |

### 可选开发（P1）

| 服务 | 接口数 | 优先级 |
|------|--------|--------|
| CategoryService | 5 | 🟡 中 |
| OrderService | 5 | 🟡 中 |

### 不开发

| 服务 | 原因 |
|------|------|
| AuthService | 需求明确排除 |

---

## Quick Reference

### API 端点

```
# 后台接口
GET    /api/admin/instructors
POST   /api/admin/instructors
PUT    /api/admin/instructors/{id}
DELETE /api/admin/instructors/{id}

GET    /api/admin/courses
POST   /api/admin/courses
PUT    /api/admin/courses/{id}
DELETE /api/admin/courses/{id}
POST   /api/admin/courses/{id}/publish
POST   /api/admin/courses/{id}/unpublish

# 前台接口
GET    /api/front/courses
GET    /api/front/courses/{id}
GET    /api/front/courses/search
GET    /api/front/instructors
GET    /api/front/instructors/{id}
```

### 数据库实体

```
users        (用户)
    ↓
instructor   (讲师)
    ↓
course        (课程)
    ↓
category      (分类)
    ↓
orders        (订单)
```

---

*生成时间：2026-11-19 | 基于设计文档综合整理*
