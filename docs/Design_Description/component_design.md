# 构件级设计文档

> 基于 `Document_Analysis.md` 第四章详细设计，细化服务构件、接口、依赖关系。

---

## 一、构件概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        表现层 (Presentation)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  后台管理页   │  │  前台展示页   │  │  API 文档    │          │
│  │  (Vue + EP)  │  │  (Vue + EP)  │  │  (Swagger)   │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
├─────────┼─────────────────┼─────────────────┼──────────────────┤
│         │     Web 层 (Controller)      │                      │
│         ├─────────────────────────────┤                      │
│         │  AdminController            │                      │
│         │  UserController             │                      │
│         │  InstructorController       │                      │
│         │  CourseController           │                      │
│         │  CategoryController         │                      │
│         │  OrderController            │                      │
│         └─────────────────────────────┘                      │
├─────────────────────────────────────────────────────────────────┤
│                        业务逻辑层 (Service)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │InstructorSvc │  │ CourseService│  │CategoryService│          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         └─────────────────┼─────────────────┘                   │
│                           │                                      │
│  ┌──────────────┐  ┌──────┴───────┐  ┌──────────────┐          │
│  │  OrderService│  │  AuthService │  │  (不开发)    │          │
│  └──────┬───────┘  └──────────────┘  └──────────────┘          │
├─────────┼──────────────────────────────────────────────────────┤
│         │           数据访问层 (DAO / Mapper)                   │
│         ├──────────────────────────────────────────────────────┤
│         │  UserMapper · InstructorMapper · CourseMapper         │
│         │  CategoryMapper · OrderMapper                         │
│         └──────────────────────────────────────────────────────┤
│                        数据存储层 (Storage)                      │
│         ┌────────────────────────────────────────────────────┐  │
│         │  PostgreSQL 18.x  +  Redis (缓存，可选)             │  │
│         └────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、服务构件详解

### 2.1 InstructorService（讲师服务）

| 属性 | 值 |
|------|-----|
| **包路径** | `com.eduplatform.service.InstructorService` |
| **职责** | 讲师信息的增删改查 |
| **依赖** | `InstructorMapper`, `CourseMapper` |
| **事务边界** | 删除讲师前需检查是否有课程关联 |

#### 接口定义

```java
public interface InstructorService {
    
    /** 查询讲师列表（分页） */
    PageResult<InstructorVO> list(InstructorQuery query);
    
    /** 查询讲师详情 */
    InstructorDetailVO getById(Long id);
    
    /** 创建讲师 */
    Long create(InstructorDTO dto);
    
    /** 更新讲师 */
    void update(Long id, InstructorDTO dto);
    
    /** 删除讲师（校验关联课程） */
    void delete(Long id);
    
    /** 查询讲师所授课程 */
    List<CourseVO> listCoursesByInstructor(Long id);
}
```

#### 关键业务规则

1. **删除校验**：删除讲师前，检查 `course.instructor_id` 是否存在关联记录，存在则抛出 `BusinessException`
2. **头像上传**：头像字段存储 URL，实际上传由前端/对象存储完成
3. **软删除**：本版本使用硬删除，可扩展为 `deleted` 字段软删除

---

### 2.2 CourseService（课程服务）

| 属性 | 值 |
|------|-----|
| **包路径** | `com.eduplatform.service.CourseService` |
| **职责** | 课程 CRUD、上下架、搜索 |
| **依赖** | `CourseMapper`, `InstructorMapper`, `CategoryMapper` |
| **事务边界** | 上下架操作需原子性 |

#### 接口定义

```java
public interface CourseService {
    
    /** 课程列表（分页 + 多条件筛选） */
    PageResult<CourseVO> list(CourseQuery query);
    
    /** 课程详情 */
    CourseDetailVO getById(Long id);
    
    /** 创建课程 */
    Long create(CourseDTO dto);
    
    /** 更新课程 */
    void update(Long id, CourseDTO dto);
    
    /** 删除课程 */
    void delete(Long id);
    
    /** 上架课程 */
    void publish(Long id);
    
    /** 下架课程 */
    void unpublish(Long id);
    
    /** 搜索课程（标题 + 描述全文搜索） */
    List<CourseVO> search(String keyword);
}
```

#### 关键业务规则

1. **状态流转**：`draft → published → archived`，不允许逆向流转
2. **价格校验**：价格必须 ≥ 0，精确到分
3. **讲师校验**：创建课程时 `instructor_id` 必须对应有效讲师
4. **分类校验**：`category_id` 可为空（未分类）

---

### 2.3 CategoryService（分类服务）

| 属性 | 值 |
|------|-----|
| **包路径** | `com.eduplatform.service.CategoryService` |
| **职责** | 分类管理（含树形结构） |
| **依赖** | `CategoryMapper` |
| **事务边界** | 删除分类前检查是否有子分类或课程关联 |

#### 接口定义

```java
public interface CategoryService {
    
    /** 查询分类树 */
    List<CategoryTreeVO> listTree();
    
    /** 查询分类列表（平铺） */
    List<CategoryVO> list(CategoryQuery query);
    
    /** 创建分类 */
    Long create(CategoryDTO dto);
    
    /** 更新分类 */
    void update(Long id, CategoryDTO dto);
    
    /** 删除分类（校验子分类和课程） */
    void delete(Long id);
}
```

#### 关键业务规则

1. **层级限制**：最大层级深度为 3（可配置）
2. **删除校验**：有子分类或课程关联时禁止删除
3. **排序**：`sort_order` 升序，同级内排序

---

### 2.4 OrderService（订单服务）

| 属性 | 值 |
|------|-----|
| **包路径** | `com.eduplatform.service.OrderService` |
| **职责** | 订单创建、支付、状态查询 |
| **依赖** | `OrderMapper`, `CourseMapper`, `UserMapper` |
| **事务边界** | 订单创建 + 库存扣减（如有） |

#### 接口定义

```java
public interface OrderService {
    
    /** 创建订单 */
    OrderDetailVO create(OrderCreateDTO dto);
    
    /** 支付订单 */
    void pay(Long orderId, String payChannel);
    
    /** 查询订单详情 */
    OrderDetailVO getById(Long id);
    
    /** 查询用户订单列表 */
    PageResult<OrderVO> listByUser(Long userId, PageQuery query);
    
    /** 取消订单 */
    void cancel(Long orderId);
}
```

#### 关键业务规则

1. **订单号生成**：`order_no` 格式：`ORDER` + 时间戳 + 随机数（如 `ORDER20261118123456789`）
2. **幂等性**：同一课程同一用户只能有一个 `pending` 订单
3. **支付模拟**：本版本使用模拟支付，直接将状态置为 `paid`

---

## 三、构件依赖关系

```
Controller
    ├── InstructorController ──→ InstructorService
    │                              ├── InstructorMapper
    │                              └── CourseMapper (关联查询)
    │
    ├── CourseController ──→ CourseService
    │                          ├── CourseMapper
    │                          ├── InstructorMapper
    │                          └── CategoryMapper
    │
    ├── CategoryController ──→ CategoryService
    │                            └── CategoryMapper
    │
    └── OrderController ──→ OrderService
                             ├── OrderMapper
                             ├── CourseMapper
                             └── UserMapper
```

---

## 四、数据传输对象（DTO / VO）

### 4.1 通用结构

```
DTO (Data Transfer Object) — 入参
├── InstructorDTO
├── CourseDTO
├── CategoryDTO
├── OrderCreateDTO
└── PageQuery, CourseQuery, InstructorQuery

VO (View Object) — 出参
├── InstructorVO / InstructorDetailVO
├── CourseVO / CourseDetailVO
├── CategoryVO / CategoryTreeVO
├── OrderVO / OrderDetailVO
└── PageResult<T>
```

### 4.2 关键字段映射

| 实体 | DTO 字段 | VO 字段 | 说明 |
|------|----------|---------|------|
| instructor | name, avatar, title, description | + id, created_at | VO 含元数据 |
| course | title, description, price, instructor_id, category_id, status | + instructor_name, category_name, tags | VO 含关联名称 |
| category | name, parent_id, sort_order, level | + children (List) | 树形结构 |
| order | user_id, course_id, amount | + user_name, course_title, order_no | VO 含展示字段 |

---

## 五、异常处理

| 异常类型 | 触发场景 | HTTP 状态码 |
|----------|----------|-------------|
| `NotFoundException` | 讲师/课程/分类/订单不存在 | 404 |
| `BusinessException` | 删除讲师有课程关联、状态流转非法 | 400 |
| `ValidationException` | 参数校验失败（价格负数、必填字段缺失） | 400 |
| `UnauthorizedException` | 未登录访问后台接口 | 401 |
| `ForbiddenException` | 无权限操作 | 403 |

---

## 六、开发范围确认

| 服务 | 是否开发 | 理由 |
|------|----------|------|
| InstructorService | ✅ 开发 | 核心业务，CRUD 完整 |
| CourseService | ✅ 开发 | 核心业务，关联讲师/分类 |
| CategoryService | ⚠️ 可选 | 可合并到 CourseService |
| OrderService | ⚠️ 可选 | 涉及支付，可用模拟实现 |
| AuthService | ❌ 不开发 | 需求明确排除 |

---

*生成时间：2026-11-19 | 基于 Document_Analysis.md*
