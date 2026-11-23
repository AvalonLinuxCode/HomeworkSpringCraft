-- ============================================================
-- 数据库设计 DDL — Spring Boot 在线教育平台
-- 数据库: PostgreSQL 18.x（已在 18.7 验证）
-- 生成时间: 2026-11-18
-- 基于: Document_Analysis.md
-- ============================================================

-- 清理已有表（开发阶段使用，生产环境请移除）
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS course CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS instructor CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 清理已有枚举
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS course_status CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;

-- ============================================================
-- 枚举类型定义
-- ============================================================
CREATE TYPE user_role AS ENUM ('admin', 'student');
CREATE TYPE course_status AS ENUM ('draft', 'published', 'archived');
CREATE TYPE order_status AS ENUM ('pending', 'paid', 'cancelled', 'refunded');

-- ============================================================
-- 用户表 (users)
-- 前台学员 + 后台管理员，通过 role 字段区分
-- ============================================================
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(200) NOT NULL,  -- BCrypt 加密存储
    email       VARCHAR(100) NOT NULL UNIQUE,
    role        user_role    NOT NULL DEFAULT 'student',
    avatar      VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS '用户表：前台学员 + 后台管理员';
COMMENT ON COLUMN users.role IS 'admin: 管理员, student: 学员';

-- ============================================================
-- 讲师表 (instructor)
-- ============================================================
CREATE TABLE instructor (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    avatar      VARCHAR(255),
    title       VARCHAR(100),           -- 头衔，如"资深Java讲师"
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE instructor IS '讲师信息表';

-- ============================================================
-- 课程分类表 (category)
-- 支持树形层级结构（parent_id 自引用）
-- ============================================================
CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    parent_id   BIGINT REFERENCES category(id) ON DELETE SET NULL,
    sort_order  INT          DEFAULT 0,
    level       INT          DEFAULT 0,      -- 层级深度，0=根节点
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE category IS '课程分类表（支持树形层级）';
COMMENT ON COLUMN category.level IS '层级深度，0 表示根节点';

-- 创建索引加速树形查询
CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_sort ON category(sort_order);

-- ============================================================
-- 课程表 (course)
-- 核心业务实体
-- ============================================================
CREATE TABLE course (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    price           DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    instructor_id   BIGINT NOT NULL REFERENCES instructor(id) ON DELETE RESTRICT,
    category_id     BIGINT REFERENCES category(id) ON DELETE SET NULL,
    status          course_status NOT NULL DEFAULT 'draft',
    cover_image     VARCHAR(255),            -- 封面图
    tags            TEXT[],                  -- PostgreSQL 数组：课程标签
    details         JSONB,                   -- PostgreSQL JSONB：动态字段（章节、评价等）
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE course IS '课程主表';
COMMENT ON COLUMN course.tags IS '课程标签数组（PostgreSQL 数组特性）';
COMMENT ON COLUMN course.details IS '课程动态字段（JSONB，含章节、评价等）';

-- 创建索引
CREATE INDEX idx_course_instructor ON course(instructor_id);
CREATE INDEX idx_course_category ON course(category_id);
CREATE INDEX idx_course_status ON course(status);
CREATE INDEX idx_course_price ON course(price);
CREATE INDEX idx_course_title ON course USING gin(to_tsvector('simple', title));

-- 关键字搜索对 description 用的是 ILIKE '%kw%'，btree 帮不上忙，需要 pg_trgm 的三元组 GIN 索引。
-- 整段包在 DO 块里：扩展不可用（未安装 contrib 或权限不足）时降级为顺序扫描并 NOTICE 提示，
-- 绝不因此中断建表——DDL 要在裸库上可重复执行。
DO $$
BEGIN
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
    CREATE INDEX IF NOT EXISTS idx_course_description_trgm
        ON course USING gin (description gin_trgm_ops);
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE '跳过 description 三元组索引（pg_trgm 不可用）: %', SQLERRM;
END
$$;

-- ============================================================
-- 订单表 (orders)
-- 记录课程购买/支付信息
-- ============================================================
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL UNIQUE,  -- 业务订单号
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    course_id       BIGINT NOT NULL REFERENCES course(id) ON DELETE RESTRICT,
    amount          DECIMAL(10, 2) NOT NULL,
    status          order_status NOT NULL DEFAULT 'pending',
    pay_time        TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE orders IS '订单/支付记录表';

-- 创建索引
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_course ON orders(course_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_no ON orders(order_no);

-- ============================================================
-- 外键约束补充（category 自引用）
-- ============================================================
ALTER TABLE category
    ADD CONSTRAINT fk_category_parent
    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL;

-- ============================================================
-- 触发器：自动更新 updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_instructor_updated_at BEFORE UPDATE ON instructor
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_category_updated_at BEFORE UPDATE ON category
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_updated_at BEFORE UPDATE ON course
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 初始数据（可选）
-- ============================================================
-- 管理员账号
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$adminhashplaceholder', 'admin@example.com', 'admin');

-- 示例分类
INSERT INTO category (name, parent_id, sort_order, level) VALUES
('前端开发', NULL, 10, 0),
('后端开发', NULL, 20, 0),
('Java', 2, 10, 1),
('Python', 2, 20, 1);

-- 示例讲师
INSERT INTO instructor (name, title, description) VALUES
('张老师', '资深Java讲师', '10年Java开发经验，擅长Spring生态'),
('李老师', '全栈工程师', '精通Vue + Spring Boot，注重实战');

-- 示例课程
INSERT INTO course (title, description, price, instructor_id, category_id, status, tags) VALUES
('Spring Boot 从入门到精通', '系统学习Spring Boot框架，掌握企业级开发', 99.99, 1, 3, 'published', ARRAY['Spring', 'Java', '后端']),
('Vue 3 实战教程', 'Vue 3 + Element Plus 构建后台管理系统', 69.99, 2, 1, 'published', ARRAY['Vue', '前端', 'Element Plus']);
