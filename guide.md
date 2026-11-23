# 本地环境安装与部署指南

> 适用：Spring Boot 在线教育平台（后端 `edu-platform/` + 前端 `edu-platform-frontend/`）
> 本章所有命令均在 **Windows + PostgreSQL 18.7** 上实测验证（v0.2.0）。

> **路径约定**：下文用 `<PROJECT_ROOT>` 指代你克隆仓库的根目录，用 `<PG_BIN>` 指代你本机 `psql.exe` 所在目录（如 `C:\Program Files\PostgreSQL\18\bin`）。**请把这两处替换成你自己的路径**——文档中出现的具体盘符只是示例，不代表你的机器。

## 一、环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+（本机 Zulu 25） | 代码编译目标 Java 21，`JAVA_HOME` 需配置 |
| Maven | 3.8+（本机 3.9.9，位于 `%USERPROFILE%\apache-maven-3.9.9`） | 构建后端 |
| Node.js | 18+（本机 v27） | 构建前端，自带 npm |
| PostgreSQL | 18.x（本机 18.7，端口 **5433**，数据目录 `AppData\postgresql_Data`） | 数据库 |

## 二、环境安装

### 1. JDK

1. 下载安装 [Eclipse Temurin / Zulu JDK 17+](https://adoptium.net/)；
2. 配置环境变量：
   - `JAVA_HOME` = JDK 安装目录（如 `C:\Program Files\Zulu\zulu-25`）
   - `PATH` 追加 `%JAVA_HOME%\bin`
3. 验证：`java -version`

### 2. Maven

1. 下载 [apache-maven-3.9.x-bin.zip](https://maven.apache.org/download.cgi) 并解压；
2. `PATH` 追加 `maven` 目录的 `bin`（如 `%USERPROFILE%\apache-maven-3.9.9\bin`）；
3. 验证：`mvn -version`（Windows 下请用 **cmd.exe**，见「常见问题」Q1）；
4. 建议在 `%USERPROFILE%\.m2\settings.xml` 配置阿里云镜像加速依赖下载。

### 3. Node.js

1. 下载安装 [Node.js 18+ LTS](https://nodejs.org/)；
2. 验证：`node -v`、`npm -v`。

### 4. PostgreSQL 18

1. 安装时选择端口 **5433**；
2. 验证：`psql -h 127.0.0.1 -p 5433 -U postgres` 可登录。

---

## 三、数据库准备（一次性）

### 1. 建业务账号与两个库（开发库 + 集成测试专用库）

```cmd
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE ROLE edu_user LOGIN PASSWORD 'edu_password';"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform OWNER edu_user;"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "CREATE DATABASE edu_platform_test OWNER edu_user;"
```

### 2. 导入 DDL（唯一事实来源：`docs/Design_Description/schema.sql`）

> ⚠️ 必须以 **edu_user** 身份执行，若用 postgres 执行，表属主是 postgres，应用会报「对表 xxx 的权限不足」。
> ⚠️ `pom.xml` 构建期会把 `docs/Design_Description/schema.sql` 拷进测试类路径，因此 **`docs/` 目录必须与 `edu-platform/` 保持同级**（默认克隆布局即满足）。若单独移动 `edu-platform/` 子目录，构建会因找不到 `schema.sql` 失败。

```cmd
cd <PG_BIN>
set PGPASSWORD=edu_password
psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -f <PROJECT_ROOT>\docs\Design_Description\schema.sql
```

DDL 自带 DROP + 建表 + 种子数据（1 用户 / 2 讲师 / 4 分类 / 2 课程），可重复执行。

---

## 四、后端

### 构建

```cmd
cd <PROJECT_ROOT>\edu-platform
mvn clean package -DskipTests     :: 产出 target\edu-platform-1.0.0-SNAPSHOT.jar
```

### 启动（开发模式）

```cmd
mvn spring-boot:run
```

或 IDEA：打开 `pom.xml` → 运行主类 `com.eduplatform.EduPlatformApplication`。

看到 `Started EduPlatformApplication` 即启动成功，监听 **http://localhost:8080**。

### 启动（部署模式 / 用打包产物）

```cmd
java -jar target\edu-platform-1.0.0-SNAPSHOT.jar
```

### 配置项（`src/main/resources/application.yml`）

默认零配置即可连接本机数据库；换环境用环境变量覆盖：

| 环境变量 | 默认值 |
|---|---|
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5433/edu_platform` |
| `DB_USER` | `edu_user` |
| `DB_PASSWORD` | `edu_password` |

> 💡 若你的 PostgreSQL 不在 **5433** 端口（本机默认 5432），设置环境变量 `DB_URL` / `TEST_DB_URL` 即可，无需改代码。`start.bat` 同样无需改——它只负责启动，不写端口。

### 停止

- 前台运行：`Ctrl+C`；
- 残留进程：`taskkill /F /PID <pid>`（pid 用 `netstat -ano | findstr 8080` 查）。

### 运行测试

```cmd
mvn test        :: Ring A 单元测试（25 个，不依赖数据库）
mvn verify      :: Ring A + Ring B 集成测试（22 个，连本机 edu_platform_test 库）
```

---

## 五、前端

### 安装依赖（首次）

```cmd
cd <PROJECT_ROOT>\edu-platform-frontend
npm install
```

### 启动（开发模式）

```cmd
npm run dev
```

监听 **http://localhost:5173**，Vite 自动把 `/api` 代理到后端 8080 —— **后端必须先启动**。

### 构建（生产产物）

```cmd
npm run build
```

产物输出到 `dist/`，可交给任意静态服务器 / Nginx 托管；同域部署时 `/api` 路径由后端承接。

### 停止

`Ctrl+C`；残留进程用任务管理器结束 node 进程。

---

## 六、一键启动（start.bat）

仓库根目录提供 `start.bat`，双击或在 **cmd.exe** 中运行：

```cmd
cd <PROJECT_ROOT>
start.bat
```

脚本流程：

1. **预检**：后端 `pom.xml` / 前端 `package.json` 是否存在，`mvn` / `node` / `npm` 是否在 PATH，端口 8080 / 5173 是否已被占用（占用仅警告）；
2. **启动后端**：新开窗口执行 `mvn spring-boot:run`（窗口标题 `EduPlatform Backend [8080]`）；
3. **等待就绪**：轮询 8080 端口直至 LISTENING（最长 `WAIT_MAX_SEC=120` 秒，超时也会继续）；
4. **启动前端**：新开窗口执行 `npm run dev`（窗口标题 `EduPlatform Frontend [5173]`），完成后打印各访问地址。

注意事项：

- 前提：数据库已按「三」准备完毕，且前端已首次执行过 `npm install`（见「五」）；
- 项目路径、端口、等待时长集中在脚本顶部「配置区」，换机器改那里即可；
- 停止服务：在两个子窗口按 `Ctrl+C`，或直接关闭窗口。

---

## 七、访问入口

| 入口 | 地址 |
|---|---|
| 前台首页 | http://localhost:5173/ |
| 课程列表 / 详情 | http://localhost:5173/courses · /courses/2 |
| 讲师列表 / 详情 | http://localhost:5173/instructors · /instructors/2 |
| 后台管理 | http://localhost:5173/admin/instructors（讲师/课程/分类） |
| 后端 API 文档 | http://localhost:8080/swagger-ui/index.html |
| 后端健康自检 | http://localhost:8080/api/front/courses |

---

## 八、常见问题

| # | 现象 | 原因与解决 |
|---|------|-----------|
| 1 | Git Bash 里 `mvn` 命令无输出、CPU 100% | Maven 脚本在 msys 下 `find_maven_basedir` 死循环。用 **cmd.exe** 执行，或先 `export MAVEN_BASEDIR='<PROJECT_ROOT>\edu-platform'` |
| 2 | 应用启动成功但接口全部 500 | 数据库连接失败：核对 PG 是否运行、`edu_user` 密码、`DB_URL` 端口。`application.yml` 默认密码 `edu_password`，可用 `DB_PASSWORD` 覆盖 |
| 3 | 报错「对表 instructor 的权限不足」 | DDL 以 postgres 身份执行过，表属主是 postgres。用 edu_user 重新执行 `schema.sql` |
| 4 | 端口被占用（8080/5173） | `netstat -ano \| findstr 8080` 查 PID → `taskkill /F /PID <pid>` |
| 5 | 前端页面打开但数据为空 | 后端未启动：Vite 代理 5173 → 8080 会失败，先起后端 |
| 6 | 中文搜索命中不全 | 全文检索用 `simple` 分词器，中文整句视为一个 token；换完整词或英文关键词搜索 |
| 7 | 集成测试连不上数据库 | 单独跑 Ring B 前确认 `edu_platform_test` 库存在；连接参数见 `edu-platform/src/test/resources/application-it.yml`（`TEST_DB_URL` 等可覆盖） |

---

*编写时间：2026-11-22 · 基于 v0.2.0 实测（mvn verify 25 单元 + 22 集成全绿，前后端真实浏览器验证通过；新增 start.bat 一键启动）*