#   Linux服务器 部署项目

```
# 0. 拉取项目代码（服务器内执行）
> 在 /data 目录拉取仓库，示例地址替换成你自己的 git 仓库地址
```bash
# 进入 /data
cd /data
# 安装git（如果还没装）
sudo apt install git -y
# 克隆仓库，替换成你的仓库地址
git clone https://github.com/AvalonLinuxCode/HomeworkSpringCraft.git
# 进入项目根目录
cd HomeworkSpringCraft
```

### 开始之前

```
# HomeworkSpringCraft 部署文档（无Nginx方案，SpringBoot内嵌静态前端）
> 环境：Debian，PostgreSQL 19，SpringBoot + Vue SPA，前端打包进SpringBoot static目录，单服务部署
> 访问方式：隧道暴露 8080 端口

## 0. 拉取项目代码（服务器内执行）
```bash
# 进入 /data
cd /data
# 安装git（如果还没装）
sudo apt install git -y
# 克隆仓库，替换为你的真实仓库地址
git clone https://github.com/AvalonLinuxCode/HomeworkSpringCraft.git
# 进入项目根目录
cd HomeworkSpringCraft
```

## 一、前置准备

```
# 更新源
sudo apt update
# 安装基础依赖
sudo apt install curl ca-certificates gpg postgresql-common maven openjdk-21-jdk -y
```

## 二、安装 PostgreSQL 19（或者指定18）

```
# 导入PG官方密钥
sudo install -d /usr/share/postgresql-common/pgdg
sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail [https://www.postgresql.org/media/keys/ACCC4CF8.asc](https://www.postgresql.org/media/keys/ACCC4CF8.asc)
# 自动添加PGDG源
sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh
# 更新缓存
sudo apt update
# 安装PG19
sudo apt install postgresql-19 postgresql-client-19 postgresql-contrib -y
```

## 三、修改PostgreSQL端口为5433

```
# 编辑配置
sudo nano /etc/postgresql/19/main/postgresql.conf
# 修改 port = 5433
# 重启PG实例（重点：不是postgresql.service，是@19-main）
sudo systemctl restart postgresql@19-main
sudo systemctl enable postgresql@19-main
# 查看状态
sudo systemctl status postgresql@19-main --no-pager
```

## 四、初始化数据库、表结构与种子数据

```
# 创建用户和数据库
sudo -u postgres psql -p 5433 -d postgres <<EOF
CREATE ROLE edu_user LOGIN PASSWORD 'edu_password';
CREATE DATABASE edu_platform OWNER edu_user;
CREATE DATABASE edu_platform_test OWNER edu_user;
EOF

# 进入项目根目录
cd /data/HomeworkSpringCraft
# 执行schema脚本
PGPASSWORD=edu_password psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -f ./docs/Design_Description/schema.sql
# 验证表是否生成
PGPASSWORD=edu_password psql -h 127.0.0.1 -p 5433 -U edu_user -d edu_platform -c "\dt"
```

## 五、前端构建 & 拷贝至SpringBoot静态目录

```
cd /data/HomeworkSpringCraft/edu-platform-frontend
npm install
npm run build

# 复制dist产物到后端static目录
cd ../edu-platform
rm -rf src/main/resources/static
mkdir -p src/main/resources/static
cp -r ../edu-platform-frontend/dist/* src/main/resources/static/
```

## 六、新增SPA路由配置（解决刷新404）

文件路径：`src/main/java/com/eduplatform/config/SPAConfig.java`

```
package com.eduplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SPAConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/**/{path:[^\\.]*}").setViewName("forward:/index.html");
    }
}
```

## 七、Maven打包SpringBoot Jar

```
cd /data/HomeworkSpringCraft/edu-platform
mvn clean package -DskipTests
# 确认jar包
ls target/*.jar
```

## 八、配置systemd托管后端服务

新建服务文件：`sudo nano /etc/systemd/system/edu-backend.service`

```
[Unit]
Description=HomeworkSpringCraft Backend SpringBoot
After=network.target postgresql@19-main.service
Requires=postgresql@19-main.service

[Service]
User=user
WorkingDirectory=/data/HomeworkSpringCraft/edu-platform
ExecStart=/usr/bin/java \
-jar target/edu-platform-1.0.0-SNAPSHOT.jar \
--server.address=127.0.0.1 \
--server.port=8080 \
--server.forward-headers-strategy=native \
-DDB_URL=jdbc:postgresql://127.0.0.1:5433/edu_platform \
-DDB_USER=edu_user \
-DDB_PASSWORD=edu_password
Restart=on-failure
RestartSec=3
StandardOutput=journal+console
StandardError=journal+console

[Install]
WantedBy=multi-user.target
```

```
# 加载配置、启动服务
sudo systemctl daemon-reload
sudo systemctl enable --now edu-backend
# 查看状态
sudo systemctl status edu-backend --no-pager
# 实时日志（排查问题）
journalctl -u edu-backend -f --no-pager
```


## 九、访问地址

- 开放相关端口，8080

- 首页： 服务器公网ip:8080


## 十、后续更新流程

### 更新前端

```
cd /data/HomeworkSpringCraft/edu-platform-frontend
npm install
npm run build
cd ../edu-platform
rm -rf src/main/resources/static
mkdir -p src/main/resources/static
cp -r ../edu-platform-frontend/dist/* src/main/resources/static/
mvn clean package -DskipTests
sudo systemctl restart edu-backend
```

### 更新后端

```
cd /data/HomeworkSpringCraft/edu-platform
mvn clean package -DskipTests
sudo systemctl restart edu-backend
```

### 更新代码（拉取最新git）

```
cd /data/HomeworkSpringCraft
git pull
```

## 十一、常用运维命令

```
# 查看后端实时日志
journalctl -u edu-backend -f --no-pager
# 重启后端
sudo systemctl restart edu-backend
# 数据库备份
PGPASSWORD=edu_password pg_dump -h 127.0.0.1 -p 5433 -U edu_user edu_platform > /data/edu_platform_$(date +%Y%m%d).sql
