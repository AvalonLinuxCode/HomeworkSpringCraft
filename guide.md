---
title: 本地环境安装与部署指南
project: Spring Boot 在线教育平台
backend: edu-platform/
frontend: edu-platform-frontend/
validated_on: Windows + PostgreSQL 18.7 (v0.2.0)
last_updated: 2026-11-23
audience: AI agent / 开发者
shell_note: 所有命令默认在 Windows cmd.exe 执行；Git Bash 下 mvn 会死循环，见 FAQ #1
---

# 本地环境安装与部署指南

本指南描述从零把项目跑起来的完整流程。**流程顺序不可调换**：环境 → 数据库 → 后端 → 前端（后端必须先于前端启动）。

## 变量约定

执行下文命令前，先确定两个占位符的真实值，替换所有出现的占位符：

| 占位符 | 含义 | 示例 | 如何确定 |
|---|---|---|---|
| `<PROJECT_ROOT>` | 仓库克隆根目录 | `E:\AllProject\TEST\HomeworkSpringCraft` | `start.bat` 会自动取脚本所在目录，无需手改；手动操作时用你的克隆路径 |
| `<PG_BIN>` | `psql.exe` 所在目录 | `C:\Program Files\PostgreSQL\18\bin` | PostgreSQL 安装目录下的 `bin` |

> 文档中出现的具体盘符均为示例，不代表执行机器的实际路径。`start.bat` 使用 `%~dp0` 自适应，跨机器免配置。

## 前置环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 代码编译目标 Java 21，`JAVA_HOME` 需配置 |
| Maven | 3.8+ | 构建后端 |
| Node.js | 18+ | 构建前端，自带 npm |
| PostgreSQL | 18.x | 数据库，端口 5433 |

---

## 任务 1：安装环境

### 1.1 JDK

```cmd
:: 安装 Eclipse Temurin / Zulu JDK 17+（https://adoptium.net/）
:: 设置环境变量：
set JAVA_HOME=<JDK安装目录>
set PATH=%PATH%;%JAVA_HOME%\bin
```

验证（成功判据：打印版本号）：

```cmd
java -version
```

### 1.2 Maven

```cmd
:: 解压 apache-maven-3.9.x-bin.zip（https://maven.apache.org/download.cgi）
:: PATH 追加 maven 目录的 bin
```

- 可选：在 `%USERPROFILE%\.m2\settings.xml` 配置阿里云镜像加速依赖下载。
- 验证（**必须用 cmd.exe**，见 FAQ #1；成功判据：打印版本号）：

```cmd
mvn -version
```

### 1.3 Node.js

安装 [Node.js 18+ LTS](https://nodejs.org/)。验证（成功判据：各打印版本号）：

```cmd
node -v
npm -v
```

### 1.4 PostgreSQL 18

- 安装时选择端口 **5433**。
- 验证（成功判据：可登录 psql）：

```cmd
psql -h 127.0.0.1 -p 5433 -U postgres
```

---

## 任务 2：数据库准备（一次性）

### 2.1 建业务账号与两个库

```cmd
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE ROLE edu_user LOGIN PASSWORD 'edu_password';"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform OWNER edu_user;"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform_test OWNER edu_user;"
```

产出：账号 `edu_user`；开发库 `edu_platform`；集成测试库 `edu_platform_test`。

### 2.2 导入 DDL

唯一事实来源：`docs/Design_Description/schema.sql`。

前置约束（违反会导致构建或权限失败）：

- 必须以 **edu_user** 身份执行。若用 postgres 执行，表属主是 postgres，应用会报「对表 xxx 的权限不足」（见 FAQ #3）。
- `pom.xml` 构建期会把 `docs/Design_Description/schema.sql` 拷进测试类路径，因此 **`docs/` 目录必须与 `edu-platform/` 保持同级**（默认克隆布局即满足）。若单独移动 `edu-platform/` 子目录，构建会因找不到 `schema.sql` 失败。

```cmd
cd <PG_BIN>
set PGPASSWORD=edu_password
psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -f <PROJECT_ROOT>\docs\Design_Description\schema.sql
```

DDL 自带 DROP + 建表 + 种子数据（1 用户 / 2 讲师 / 4 分类 / 2 课程），可重复执行（幂等）。

---

## 任务 3：后端

### 3.1 构建

```cmd
cd <PROJECT_ROOT>\edu-platform
mvn clean package -DskipTests
```

产出：`target\edu-platform-1.0.0-SNAPSHOT.jar`。

### 3.2 启动（开发模式）

```cmd
mvn spring-boot:run
```

- 或 IDEA：打开 `pom.xml` → 运行主类 `com.eduplatform.EduPlatformApplication`。
- 成功判据：日志出现 `Started EduPlatformApplication`，监听 `http://localhost:8080`。

### 3.3 启动（部署模式 / 用打包产物）

```cmd
java -jar target\edu-platform-1.0.0-SNAPSHOT.jar
```

### 3.4 配置项

文件：`src/main/resources/application.yml`。默认零配置即可连接本机数据库；换环境用环境变量覆盖：

| 环境变量 | 默认值 |
|---|---|
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5433/edu_platform` |
| `DB_USER` | `edu_user` |
| `DB_PASSWORD` | `edu_password` |

> PostgreSQL 不在 **5433** 端口时（PG 默认 5432），设置 `DB_URL` / `TEST_DB_URL` 即可，无需改代码。`start.bat` 只负责启动，不写端口，同样无需改。

### 3.5 停止

- 前台运行：`Ctrl+C`
- 残留进程：`taskkill /F /PID <pid>`（pid 用 `netstat -ano | findstr 8080` 查，见 FAQ #4）

### 3.6 运行测试

```cmd
mvn test        :: Ring A 单元测试（25 个，不依赖数据库）
mvn verify      :: Ring A + Ring B 集成测试（22 个，连本机 edu_platform_test 库）
```

---

## 任务 4：前端

### 4.1 安装依赖（首次）

```cmd
cd <PROJECT_ROOT>\edu-platform-frontend
npm install
```

### 4.2 启动（开发模式）

前置：**后端必须已启动**（Vite 把 `/api` 代理到 8080，后端未起则数据为空，见 FAQ #5）。

```cmd
npm run dev
```

成功判据：监听 `http://localhost:5173`。

### 4.3 构建（生产产物）

```cmd
npm run build
```

产出：`dist/`，可交给任意静态服务器 / Nginx 托管；同域部署时 `/api` 路径由后端承接。

### 4.4 停止

`Ctrl+C`；残留进程用任务管理器结束 node 进程。

---

## 任务 5：一键启动（start.bat）

`start.bat` 位于仓库根目录，双击或在 **cmd.exe** 中运行：

```cmd
cd <PROJECT_ROOT>
start.bat
```

脚本流程（顺序固定）：

1. **预检**：后端 `pom.xml` / 前端 `package.json` 是否存在；`mvn` / `node` / `npm` 是否在 PATH；端口 8080 / 5173 是否被占用（占用仅警告，不阻断）。
2. **启动后端**：新开窗口执行 `mvn spring-boot:run`（窗口标题 `EduPlatform Backend [8080]`）。
3. **等待就绪**：轮询 8080 端口直至 LISTENING（最长 `WAIT_MAX_SEC=120` 秒，超时也会继续）。
4. **启动前端**：新开窗口执行 `npm run dev`（窗口标题 `EduPlatform Frontend [5173]`），完成后打印各访问地址。

前置条件：数据库已按任务 2 准备完毕，且前端已首次执行过 `npm install`（任务 4.1）。

可配置项：项目路径、端口、等待时长集中在脚本顶部「配置区」。项目路径用 `%~dp0` 自动取脚本所在目录，跨机器免改；如需覆盖可在该处手动 `set`。

停止服务：在两个子窗口按 `Ctrl+C`，或直接关闭窗口。

---

## 访问入口

| 入口 | 地址 |
|---|---|
| 前台首页 | http://localhost:5173/ |
| 课程列表 / 详情 | http://localhost:5173/courses · /courses/2 |
| 讲师列表 / 详情 | http://localhost:5173/instructors · /instructors/2 |
| 后台管理 | http://localhost:5173/admin/instructors（讲师/课程/分类） |
| 后端 API 文档 | http://localhost:8080/swagger-ui/index.html |
| 后端健康自检 | http://localhost:8080/api/front/courses |

---

## 常见问题（FAQ）

| # | 现象 | 原因 | 解决 |
|---|------|------|------|
| 1 | Git Bash 里 `mvn` 命令无输出、CPU 100% | Maven 脚本在 msys 下 `find_maven_basedir` 死循环 | 用 **cmd.exe** 执行，或先 `export MAVEN_BASEDIR='<PROJECT_ROOT>\edu-platform'` |
| 2 | 应用启动成功但接口全部 500 | 数据库连接失败 | 核对 PG 是否运行、`edu_user` 密码、`DB_URL` 端口；`application.yml` 默认密码 `edu_password`，可用 `DB_PASSWORD` 覆盖 |
| 3 | 报错「对表 instructor 的权限不足」 | DDL 以 postgres 身份执行过，表属主是 postgres | 用 edu_user 重新执行 `schema.sql` |
| 4 | 端口被占用（8080/5173） | 端口冲突 | `netstat -ano \| findstr 8080` 查 PID → `taskkill /F /PID <pid>` |
| 5 | 前端页面打开但数据为空 | 后端未启动，Vite 代理 5173→8080 失败 | 先起后端 |
| 6 | 中文搜索命中不全 | 全文检索用 `simple` 分词器，中文整句视为一个 token | 换完整词或英文关键词搜索 |
| 7 | 集成测试连不上数据库 | `edu_platform_test` 库不存在 | 单独跑 Ring B 前确认该库存在；连接参数见 `edu-platform/src/test/resources/application-it.yml`（`TEST_DB_URL` 等可覆盖） |

---

*基于 v0.2.0 实测（mvn verify 25 单元 + 22 集成全绿，前后端真实浏览器验证通过；含 start.bat 一键启动）。2026-11-23 重写为 Agent 友好范式，流程与命令未变。*
