# LifeCompass EC2 Docker 部署文档

本文档对应当前选择的部署方式：在一台 AWS EC2 上使用 Docker Compose 运行全部后端与数据功能。

## 1. 部署架构

```text
EC2 Ubuntu
├── nginx:1.27-alpine          对外暴露 80，反向代理 API/图片/上传文件
├── life-compass-backend       Spring Boot 后端
├── mysql:8.4                  MySQL 数据库
├── redis:7-alpine             Redis 缓存、短信验证码、GEO 索引
├── mysql-data volume          MySQL 持久化数据
├── redis-data volume          Redis AOF 持久化数据
└── uploads-data volume        用户上传图片
```

前端仍建议部署在 Vercel、Cloudflare Pages 或 S3 + CloudFront；前端通过环境变量访问 EC2 后端：

```text
VITE_API_BASE_URL=http://<EC2_PUBLIC_IP>/api
VITE_ASSET_BASE_URL=http://<EC2_PUBLIC_IP>
```

配置域名和 HTTPS 后可改为：

```text
VITE_API_BASE_URL=https://api.your-domain.com/api
VITE_ASSET_BASE_URL=https://api.your-domain.com
```

## 2. 本次新增/修改的部署文件

### 2.1 `.dockerignore`

用途：

- 排除 `.git`、IDE 文件、`target/`、`frontend/node_modules/`、`uploads/` 等无关内容。
- 排除 `src/main/resources/application.yaml`，防止本地私密配置被打进 Docker 镜像。
- 保留 `.env.ec2.example` 作为生产环境变量模板。

### 2.2 `Dockerfile`

用途：

- 使用 `eclipse-temurin:17-jdk-jammy` 构建 Spring Boot JAR。
- 使用 `eclipse-temurin:17-jre-jammy` 作为运行镜像。
- 默认启用 `SPRING_PROFILES_ACTIVE=prod`。
- 将上传目录固定为容器内 `/app/uploads`，由 Docker volume 持久化。

### 2.3 `docker-compose.ec2.yml`

包含 4 个服务：

| 服务 | 说明 |
|---|---|
| `mysql` | MySQL 8.4，首次启动自动执行 `sql/schema.sql` 和 `sql/data.sql` |
| `redis` | Redis 7，开启 AOF 持久化 |
| `backend` | Spring Boot 后端，连接 compose 网络中的 `mysql` 和 `redis` |
| `nginx` | 对外暴露 80，代理 `/api`、`/images`、`/uploads`、Swagger |

### 2.4 `nginx/conf.d/life-compass.conf`

用途：

- `GET /health` 返回 `ok`，用于检查 Nginx 是否可访问。
- `/api/**` 代理到 Spring Boot。
- `/images/**` 和 `/uploads/**` 代理到 Spring Boot 静态资源。
- `/swagger-ui/**` 和 `/v3/api-docs/**` 代理到后端接口文档。

### 2.5 `.env.ec2.example`

用途：

- 作为 EC2 上 `.env` 的模板。
- 保存数据库密码、JWT 密钥、CORS 前端域名和第三方 API key。
- 真实 `.env` 不应提交到 Git。

### 2.6 `src/main/resources/application-prod.yaml`

用途：

- Docker 环境专用 Spring Boot 配置。
- 通过环境变量读取 MySQL、Redis、JWT、CORS、Google、Twilio、DeepSeek 配置。
- 避免依赖本地 `application.yaml`。

## 3. EC2 准备步骤

### 3.1 创建 EC2

建议配置：

```text
AMI: Ubuntu 22.04 LTS 或 Ubuntu 24.04 LTS
Instance: t3.small 或以上
Storage: 20GB+ gp3 EBS
Security Group:
  22  only your IP
  80  0.0.0.0/0
  443 0.0.0.0/0  # 配 HTTPS 时使用
```

不要开放：

```text
3306
6379
8080
```

这些端口只在 Docker 内部网络使用。

### 3.2 安装 Docker

SSH 登录 EC2 后执行：

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker ubuntu
```

退出 SSH 后重新登录，让 `docker` 用户组生效。

验证：

```bash
docker --version
docker compose version
```

## 4. 上传项目到 EC2

方式一：GitHub 拉取：

```bash
sudo mkdir -p /opt/life-compass
sudo chown -R ubuntu:ubuntu /opt/life-compass
cd /opt/life-compass
git clone <your-repo-url> .
```

方式二：本地打包上传：

```bash
scp -i <key.pem> -r life-compass ubuntu@<EC2_PUBLIC_IP>:/opt/life-compass
```

进入目录：

```bash
cd /opt/life-compass
```

## 5. 配置生产环境变量

```bash
cp .env.ec2.example .env
nano .env
```

至少修改：

```text
DB_PASSWORD=<strong-db-password>
MYSQL_ROOT_PASSWORD=<strong-root-password>
LIFECOMPASS_JWT_SECRET=<32+ bytes random secret>
LIFECOMPASS_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

生成 JWT 密钥：

```bash
openssl rand -base64 48
```

如果前端还没部署，可以先临时写：

```text
LIFECOMPASS_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://<EC2_PUBLIC_IP>
```

## 6. 启动后端、MySQL、Redis、Nginx

首次启动：

```bash
docker compose -f docker-compose.ec2.yml up -d --build
```

查看状态：

```bash
docker compose -f docker-compose.ec2.yml ps
```

查看日志：

```bash
docker compose -f docker-compose.ec2.yml logs -f backend
docker compose -f docker-compose.ec2.yml logs -f mysql
docker compose -f docker-compose.ec2.yml logs -f nginx
```

## 7. 验证部署

Nginx 健康检查：

```bash
curl http://<EC2_PUBLIC_IP>/health
```

后端公开接口：

```bash
curl http://<EC2_PUBLIC_IP>/api/auth/config
curl http://<EC2_PUBLIC_IP>/api/shop
```

Swagger：

```text
http://<EC2_PUBLIC_IP>/swagger-ui/index.html
```

MySQL 是否初始化成功：

```bash
docker compose -f docker-compose.ec2.yml exec mysql mysql -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" -e "SHOW TABLES;"
```

Redis 是否可用：

```bash
docker compose -f docker-compose.ec2.yml exec redis redis-cli ping
```

## 8. 前端生产环境变量

如果前端部署在 Vercel/Cloudflare Pages：

```text
VITE_API_BASE_URL=http://<EC2_PUBLIC_IP>/api
VITE_ASSET_BASE_URL=http://<EC2_PUBLIC_IP>
```

配置域名 HTTPS 后：

```text
VITE_API_BASE_URL=https://api.your-domain.com/api
VITE_ASSET_BASE_URL=https://api.your-domain.com
```

前端环境变量改完后需要重新构建/重新部署前端。

## 9. 常用运维命令

更新代码并重新部署：

```bash
cd /opt/life-compass
git pull
docker compose -f docker-compose.ec2.yml up -d --build
```

重启：

```bash
docker compose -f docker-compose.ec2.yml restart
```

停止：

```bash
docker compose -f docker-compose.ec2.yml down
```

停止并删除数据卷，谨慎使用：

```bash
docker compose -f docker-compose.ec2.yml down -v
```

## 10. 数据备份

备份 MySQL：

```bash
mkdir -p backups
docker compose -f docker-compose.ec2.yml exec -T mysql \
  mysqldump -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" > backups/lifecompass-$(date +%F).sql
```

备份上传文件：

```bash
docker run --rm \
  -v life-compass_uploads-data:/data \
  -v "$PWD/backups:/backup" \
  alpine tar czf /backup/uploads-$(date +%F).tar.gz -C /data .
```

建议把 `backups/` 定期同步到 S3 或下载到本地。

## 11. HTTPS 后续配置

当前 compose 默认只开放 HTTP 80。正式上线建议：

1. 域名 DNS A 记录指向 EC2 公网 IP。
2. 使用 Nginx + Certbot 配置 Let's Encrypt。
3. 或者在 EC2 前面放 AWS Application Load Balancer，用 ACM 托管证书。

完成 HTTPS 后记得同步修改：

```text
VITE_API_BASE_URL=https://api.your-domain.com/api
VITE_ASSET_BASE_URL=https://api.your-domain.com
LIFECOMPASS_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

## 12. 重要注意事项

### 12.1 MySQL 初始化只在第一次创建 volume 时执行

`sql/schema.sql` 和 `sql/data.sql` 只会在 `mysql-data` volume 为空时自动执行。之后即使 SQL 文件变化，MySQL 容器也不会重新导入。

如果是测试环境想重置数据库：

```bash
docker compose -f docker-compose.ec2.yml down -v
docker compose -f docker-compose.ec2.yml up -d --build
```

注意：这会删除数据库、Redis 和上传文件 volume。

### 12.2 uploads 已持久化，但仍建议未来迁移到 S3

当前 `uploads-data` volume 能在容器重建后保留上传文件，但它仍依赖这台 EC2 和这块 EBS。正式生产更推荐 S3 + CloudFront。

### 12.3 不要开放数据库和 Redis 端口

MySQL 和 Redis 只需要容器内部访问。EC2 安全组不应开放 3306 和 6379。

## 13. 本次已执行的本地验证

### 13.1 Docker Compose 配置校验

已执行：

```bash
docker compose -f docker-compose.ec2.yml --env-file .env.ec2.example config
```

结果：

- 通过。
- Compose 能正确解析 `mysql`、`redis`、`backend`、`nginx` 四个服务。
- 环境变量能从 `.env.ec2.example` 注入。
- MySQL、Redis、uploads 三个 volume 能正确生成。

### 13.2 后端 Docker 镜像构建

已执行：

```bash
DOCKER_CONFIG=/tmp/life-compass-docker docker compose -f docker-compose.ec2.yml --env-file .env.ec2.example build backend
```

结果：

- 通过。
- Maven 在 Docker build 阶段成功编译 99 个 Java source files。
- 生成并打包 `/workspace/target/life-compass-0.0.1-SNAPSHOT.jar`。
- 成功构建镜像 `life-compass-backend:latest`。

说明：

- 当前本地环境需要把 `DOCKER_CONFIG` 指到 `/tmp`，因为默认 `$HOME/.docker` 在沙箱中不可写。
- 在 EC2 上通常不需要加 `DOCKER_CONFIG=/tmp/life-compass-docker`。

### 13.3 未执行完整 `up`

本次没有在当前机器执行：

```bash
docker compose -f docker-compose.ec2.yml up -d
```

原因：

- 当前机器不是目标 EC2 环境。
- 完整启动会创建本地 MySQL/Redis/uploads 持久化 volume，并占用 80 端口。
- 已通过 `config` 和 `build backend` 验证部署文件结构与后端镜像构建链路。
