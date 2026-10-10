# BabyCare MiniApp

这是一个用于记录婴儿日常护理的微信小程序 + Java 后端项目，适合记录：

- 喂奶
- 尿便/拉粑
- 睡眠
- 体温
- 备注
- 微信提醒

## 技术栈

- Java 17
- Spring Boot 3.3.x
- MyBatis-Plus
- MySQL 8+
- Flyway
- 微信小程序

## 项目结构

```text
backend/
  pom.xml
  src/main/assembly/assembly.xml
  src/main/assembly/bin/
  src/main/resources/bootstrap.yml
  src/main/resources/log4j2-spring.xml
  src/main/java/com/example/babycare/...
  src/main/resources/application.yml
  src/main/resources/db/migration/V1__init.sql
miniprogram/
  app.json
  pages/index/index.js
  pages/index/index.wxml
  pages/index/index.wxss
```

## 数据库

本项目使用 MySQL + Flyway 管理结构。
Flyway 执行记录保存在 `babycare_server_VERSION` 表。旧的 `flyway_schema_history` 不会自动改动；已有业务表的数据库首次使用新记录表时会按现有 `baseline-on-migrate` 配置建立基线。

### 本地启动 MySQL

```bash
docker run --name babycare-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=babycare \
  -p 3306:3306 \
  -d mysql:8.0
```

## 后端启动

先在 Nacos 的 `xc` 命名空间（使用实际命名空间 ID）、`DEFAULT_GROUP` 下创建 Data ID `common-wxapp`，配置内容采用 YAML 格式：

```yaml
WXAPP_DB_CLASS_NAME: com.mysql.cj.jdbc.Driver
WXAPP_DB_URL: jdbc:mysql://localhost:3306/babycare?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
WXAPP_DB_USERNAME: root
WXAPP_DB_PASSWORD: "你的数据库密码"
```

Nacos 连接参数在 `bootstrap.yml` 中配置，可通过 `NACOS_SERVER_ADDR`、`NACOS_NAMESPACE`、`NACOS_USERNAME` 和 `NACOS_PASSWORD` 环境变量覆盖。数据库连接在启动时从 Nacos 读取；配置缺失或 Nacos 不可用时，应用会启动失败。

```bash
cd backend
mvn clean package
tar -xzf target/babycare-server.tar.gz
./babycare-server/bin/start.sh
./babycare-server/bin/status.sh
./babycare-server/bin/stop.sh
```

分发包包含 `bin/`、`boot/`、`lib/` 和 `config/`。`bin/boot.config` 默认设置 `-Xms500m -Xmx500m`；也可用 `restart.sh` 重启。当前应用日志写到分发包上级目录的 `xcAppLogs/babycare-server-logs/info.log`；跨日后的下一条日志或单文件达到 200 MB 时，旧日志会压缩成 `info-yyyy-MM-dd-序号.log.gz`，新日志继续写入 `info.log`。同一天最多保留 10 个压缩归档。手工运行时可编辑分发包内的 `config/bootstrap.yml`；自动部署时请使用下文的服务器环境文件或配置覆盖目录。

## GitHub Actions 自动部署后端

仓库中的 [部署工作流](.github/workflows/deploy-backend.yml) 仅在 GitHub 的 **Actions → Deploy backend → Run workflow** 手动触发；普通代码提交不会自动部署。服务器需要能通过 SSH 访问，并预装 Java 17、`bash`、`tar` 和 `curl`。部署账号需要能写入家目录（暂存上传包）及正式部署目录的上级目录。

1. 在服务器创建专用部署账号，并将部署公钥添加到该账号的 `~/.ssh/authorized_keys`。在可信环境核对服务器 SSH 主机密钥指纹后，将对应的 `known_hosts` 条目用于下面的 Secret。

   可在本地执行 `ssh-keygen -t ed25519 -f ~/.ssh/babycare_deploy -N ''` 创建专用密钥；将 `babycare_deploy.pub` 的内容添加到服务器账号的 `authorized_keys`，将 `babycare_deploy` 的完整内容添加到 `DEPLOY_SSH_KEY`。`DEPLOY_KNOWN_HOSTS` 可从 `ssh-keyscan -H 服务器地址` 获取，填写前须与服务器实际指纹核对；自定义 SSH 端口时使用 `ssh-keyscan -p 端口 -H 服务器地址`。
2. 在仓库 **Settings → Environments → qiniuyun → Environment secrets** 中添加以下环境 Secrets。部署工作流已绑定 `qiniuyun` 环境：

   | 名称 | 内容 |
   | --- | --- |
   | `DEPLOY_HOST` | 服务器 SSH 地址或域名 |
   | `DEPLOY_USER` | 服务器部署账号 |
   | `DEPLOY_SSH_KEY` | 对应的 SSH 私钥原文，包含首尾标记和换行 |
   | `DEPLOY_KNOWN_HOSTS` | 已核对指纹的服务器 `known_hosts` 条目 |

   SSH 端口不是 22 时，在同一环境的 **Environment variables** 中添加 `DEPLOY_PORT`。可在这里添加 `DEPLOY_DIR` 设置服务器上的完整部署目录，默认是 `/data/babycare-server`；修改路径无需改代码。
3. 确认服务器本机 `127.0.0.1:8848` 的 Nacos 中已有命名空间 ID `xc`、`DEFAULT_GROUP` 下的 `common-wxapp` 配置，包含 `WXAPP_DB_CLASS_NAME`、`WXAPP_DB_URL`、`WXAPP_DB_USERNAME` 和 `WXAPP_DB_PASSWORD`。如果命名空间 ID 不同，在部署目录的上级目录创建 `babycare-server.env`，例如：

   ```bash
   NACOS_NAMESPACE=实际命名空间ID
   NACOS_USERNAME=实际用户名
   NACOS_PASSWORD=实际密码
   ```

   此文件是由启动脚本读取的 Shell 配置，默认路径为 `/data/babycare-server.env`，建议设置权限为 `600`。Nacos 和 MySQL 都位于服务器本机时，Nacos 内的 `WXAPP_DB_URL` 应指向服务器本机的 MySQL 地址。需要覆盖随包配置时，可把配置文件放在部署目录上级目录的 `babycare-config/` 中，部署时会复制到新版本的 `config/` 目录。
4. 将本工作流和脚本提交并推送到 `main`。默认程序路径为 `/data/babycare-server/`，日志为 `/data/xcAppLogs/babycare-server-logs/`，历史版本为 `/data/babycare-backups/`；设置 `DEPLOY_DIR` 后，这些目录随之移动。部署脚本通过本机 `8080` 端口检查启动结果；如果应用启动失败，会尝试恢复并启动上一版本。如先前在家目录启动过旧版本，迁移前先停止旧实例。

SSH 私钥只保存在 GitHub Secret 中；服务器上的 `babycare-server.env`、Nacos 数据库密码和个性化配置无需提交到仓库。

## 微信小程序说明

1. 在微信开发者工具中打开 `miniprogram` 目录。
2. 配置自己的 AppID。
3. 替换后端地址为你的实际部署地址。
4. 在微信后台配置订阅消息模板。

## 默认提醒规则

- 3 小时未记录喂奶提醒
- 4 小时未记录尿便提醒

## 注意事项

- `application.yml` 中的微信配置需要替换为自己账号的 AppID / Secret / Template ID。
- 真实部署前，需要把后端地址和微信域名配置好。

## 版权

MIT
